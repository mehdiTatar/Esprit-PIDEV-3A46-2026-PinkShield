<?php

namespace App\Controller;

use App\Entity\Appointment;
use App\Entity\User;
use App\Entity\Doctor;
use App\Entity\Notification;
use App\Repository\AppointmentRepository;
use App\Repository\DoctorRepository;
use App\Repository\DailyTrackingRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use App\Form\ParapharmacieType;
use App\Form\AppointmentFormType;
use App\Entity\Parapharmacie;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use TCPDF;
use App\Service\AppointmentMailer;
use App\Service\SmsNotificationService;
use App\Service\AiSymptomAnalyzerService;

#[Route('/appointment')]
class AppointmentController extends AbstractController
{
    private SmsNotificationService $smsNotificationService;
    private AiSymptomAnalyzerService $aiSymptomAnalyzer;

    public function __construct(SmsNotificationService $smsNotificationService, AiSymptomAnalyzerService $aiSymptomAnalyzer)
    {
        $this->smsNotificationService = $smsNotificationService;
        $this->aiSymptomAnalyzer = $aiSymptomAnalyzer;
    }

    #[Route('/', name: 'appointment_index')]
    public function index(AppointmentRepository $appointmentRepository): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $user = $this->getUser();
        $appointments = [];

        if ($this->isGranted('ROLE_ADMIN')) {
            $appointments = $appointmentRepository->findAll();
        } elseif ($this->isGranted('ROLE_DOCTOR')) {
            $appointments = $appointmentRepository->findByDoctor($user->getUserIdentifier());
        } elseif ($this->isGranted('ROLE_USER')) {
            $appointments = $appointmentRepository->findByPatient($user->getUserIdentifier());
        }

        return $this->render('appointment/index.html.twig', [
            'appointments' => $appointments,
        ]);
    }

    #[Route('/fc/load-events', name: 'fc_load_events', methods: ['GET'])]
    public function loadEvents(AppointmentRepository $appointmentRepository): JsonResponse
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $user = $this->getUser();
        $appointments = [];

        if ($this->isGranted('ROLE_ADMIN')) {
            $appointments = $appointmentRepository->findAll();
        } elseif ($this->isGranted('ROLE_DOCTOR')) {
            $appointments = $appointmentRepository->findByDoctor($user->getUserIdentifier());
        } elseif ($this->isGranted('ROLE_USER')) {
            $appointments = $appointmentRepository->findByPatient($user->getUserIdentifier());
        }

        // Filter out cancelled appointments and format as FullCalendar events
        $events = [];
        foreach ($appointments as $appointment) {
            if ($appointment->getStatus() !== 'cancelled') {
                $appointmentDateTime = $appointment->getAppointmentDate();
                if ($appointmentDateTime instanceof \DateTime) {
                    $endDate = (clone $appointmentDateTime)->modify('+30 minutes');
                } else {
                    $endDate = \DateTime::createFromInterface($appointmentDateTime)->modify('+30 minutes');
                }
                
                $events[] = [
                    'id' => $appointment->getId(),
                    'title' => $appointment->getPatientName() . ' (Dr. ' . $appointment->getDoctorName() . ')',
                    'start' => $appointment->getAppointmentDate()->format('Y-m-d\TH:i:s'),
                    'end' => $endDate->format('Y-m-d\TH:i:s'),
                    'backgroundColor' => $this->getEventColor($appointment->getStatus()),
                    'borderColor' => $this->getEventBorderColor($appointment->getStatus()),
                    'textColor' => '#ffffff',
                    'extendedProps' => [
                        'patientEmail' => $appointment->getPatientEmail(),
                        'patientName' => $appointment->getPatientName(),
                        'doctorName' => $appointment->getDoctorName(),
                        'doctorEmail' => $appointment->getDoctorEmail(),
                        'status' => $appointment->getStatus(),
                        'notes' => $appointment->getNotes(),
                    ],
                ];
            }
        }

        return $this->json($events);
    }

    #[Route('/new', name: 'appointment_new')]
    public function new(Request $request, DoctorRepository $doctorRepository, UserRepository $userRepository, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        // Only patients can book appointments
        if ($this->isGranted('ROLE_DOCTOR') || $this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Only patients can book appointments.');
            return $this->redirectToRoute('appointment_index');
        }

        $doctors = $doctorRepository->findAll();

        $appointment = new Appointment();
        $currentUser = $this->getUser();
        $appointment->setPatientEmail($currentUser->getUserIdentifier());
        $patientName = ($currentUser instanceof User) ? $currentUser->getFullName() : 'Patient';
        $appointment->setPatientName($patientName);
        $appointment->setStatus('pending');

        $form = $this->createForm(AppointmentFormType::class, $appointment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Ensure appointmentDate is not null
            if (!$appointment->getAppointmentDate()) {
                $this->addFlash('error', 'Appointment date is required and must be in the future.');
                return $this->render('appointment/new.html.twig', [
                    'form' => $form->createView(),
                    'doctors' => $doctors,
                ]);
            }

            // Set doctor name from the selected doctor email
            if ($appointment->getDoctorEmail()) {
                $doctor = $doctorRepository->findOneBy(['email' => $appointment->getDoctorEmail()]);
                if ($doctor) {
                    $appointment->setDoctorName($doctor->getFullName());
                }
            }

            $entityManager->persist($appointment);
            $entityManager->flush();

            // Send SMS confirmation to patient
            try {
                $patientUser = $userRepository->findOneBy(['email' => $appointment->getPatientEmail()]);
                if ($patientUser && $patientUser->getPhone()) {
                    $this->smsNotificationService->sendAppointmentConfirmation(
                        $patientUser->getPhone(),
                        $appointment->getAppointmentDate(),
                        $appointment->getDoctorName()
                    );
                }
            } catch (\Exception $e) {
                // Log SMS error but don't fail the appointment creation
                error_log("SMS notification failed: " . $e->getMessage());
            }

            // Create notification for all admins
            $admins = $userRepository->findByRole('ROLE_ADMIN');
            foreach ($admins as $admin) {
                $notification = new Notification();
                $notification->setUser($admin);
                $notification->setTitle('New Appointment Booking');
                $notification->setMessage($appointment->getPatientName() . ' booked an appointment with ' . $appointment->getDoctorName() . ' on ' . $appointment->getAppointmentDate()->format('Y-m-d H:i'));
                $notification->setType('info');
                $notification->setIcon('fas fa-calendar-check');
                $entityManager->persist($notification);
            }
            $entityManager->flush();

            $this->addFlash('success', 'Appointment booked successfully! Waiting for doctor confirmation.');
            return $this->redirectToRoute('appointment_index');
        }

        return $this->render('appointment/new.html.twig', [
            'form' => $form->createView(),
            'doctors' => $doctors,
        ]);
    }

    #[Route('/{id}/confirm', name: 'appointment_confirm')]
    #[IsGranted('ROLE_DOCTOR')]
    public function confirm(Appointment $appointment, EntityManagerInterface $entityManager, UserRepository $userRepository): Response
    {
        if ($appointment->getDoctorEmail() !== $this->getUser()->getUserIdentifier() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException();
        }

        $appointment->setStatus('confirmed');
        $entityManager->flush();

        // Send SMS confirmation to patient when doctor confirms
        try {
            $patientUser = $userRepository->findOneBy(['email' => $appointment->getPatientEmail()]);
            if ($patientUser && $patientUser->getPhone()) {
                $this->smsNotificationService->sendAppointmentConfirmation(
                    $patientUser->getPhone(),
                    $appointment->getAppointmentDate(),
                    $appointment->getDoctorName()
                );
            }
        } catch (\Exception $e) {
            // Log SMS error but don't fail the confirmation
            error_log("SMS notification failed: " . $e->getMessage());
        }

        $this->addFlash('success', 'Appointment confirmed.');
        return $this->redirectToRoute('appointment_index');
    }

    #[Route('/{id}', name: 'appointment_show')]
    public function show(Appointment $appointment, Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // Access: patient who owns, doctor assigned, or admin
        $userEmail = $this->getUser()->getUserIdentifier();
        if ($appointment->getPatientEmail() !== $userEmail && $appointment->getDoctorEmail() !== $userEmail && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException();
        }

        // Handle Parapharmacie add form (doctors/admin)
        $paraph = new Parapharmacie();
        $form = $this->createForm(ParapharmacieType::class, $paraph);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $paraph->setAppointment($appointment);
            $entityManager->persist($paraph);
            $entityManager->flush();
            $this->addFlash('success', 'Parapharmacie item added.');
            return $this->redirectToRoute('appointment_show', ['id' => $appointment->getId()]);
        }

        return $this->render('appointment/show.html.twig', [
            'appointment' => $appointment,
            'paraphForm' => $form->createView(),
        ]);
    }

    #[Route('/{id}/edit', name: 'appointment_edit')]
    public function edit(Appointment $appointment, Request $request, EntityManagerInterface $entityManager, UserRepository $userRepository): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $userEmail = $this->getUser()->getUserIdentifier();
        // Only admin or owner (patient) or doctor assigned can edit
        if ($appointment->getPatientEmail() !== $userEmail && $appointment->getDoctorEmail() !== $userEmail && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException();
        }

        if ($request->isMethod('POST')) {
            $dateStr = $request->request->get('date');
            $notes = $request->request->get('notes');
            $status = $request->request->get('status');
            $oldStatus = $appointment->getStatus();
            
            if ($dateStr) {
                $appointment->setAppointmentDate(new \DateTime($dateStr));
            }
            $appointment->setNotes($notes);
            if ($status) {
                $appointment->setStatus($status);
            }
            $entityManager->flush();

            // Send SMS notification if status changed to completed
            if ($oldStatus !== 'completed' && $status === 'completed') {
                try {
                    $patientUser = $userRepository->findOneBy(['email' => $appointment->getPatientEmail()]);
                    if ($patientUser && $patientUser->getPhone()) {
                        $this->smsNotificationService->sendAppointmentCompletion(
                            $patientUser->getPhone(),
                            $appointment->getDoctorName()
                        );
                    }
                } catch (\Exception $e) {
                    // Log SMS error but don't fail the appointment update
                    error_log("SMS notification failed: " . $e->getMessage());
                }
            }

            $this->addFlash('success', 'Appointment updated.');
            return $this->redirectToRoute('appointment_show', ['id' => $appointment->getId()]);
        }

        return $this->render('appointment/edit.html.twig', [
            'appointment' => $appointment,
        ]);
    }

    #[Route('/{id}/cancel', name: 'appointment_cancel')]
    public function cancel(Appointment $appointment, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $userEmail = $this->getUser()->getUserIdentifier();
        
        if ($appointment->getPatientEmail() !== $userEmail && 
            $appointment->getDoctorEmail() !== $userEmail && 
            !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException();
        }

        $appointment->setStatus('cancelled');
        $entityManager->flush();

        $this->addFlash('success', 'Appointment cancelled.');
        return $this->redirectToRoute('appointment_index');
    }

    #[Route('/{id}/invoice', name: 'appointment_invoice')]
    public function generateInvoice(Appointment $appointment): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $userEmail = $this->getUser()->getUserIdentifier();
        
        // Access: patient, doctor, or admin
        if ($appointment->getPatientEmail() !== $userEmail && 
            $appointment->getDoctorEmail() !== $userEmail && 
            !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException();
        }

        // Calculate invoice details
        $invoiceNumber = 'INV-' . $appointment->getId() . '-' . $appointment->getAppointmentDate()->format('Ymd');
        $invoiceDate = new \DateTime();
        $total = $this->calculateTotal($appointment);

        // Create PDF
        $pdf = new TCPDF();
        $pdf->SetCreator(PDF_CREATOR);
        $pdf->SetAuthor('PinkShield Medical Services');
        $pdf->SetTitle('Invoice ' . $invoiceNumber);
        $pdf->SetDefaultMonospacedFont(PDF_FONT_MONOSPACED);
        $pdf->SetMargins(15, 15, 15);
        $pdf->SetAutoPageBreak(TRUE, 15);
        $pdf->AddPage();
        $pdf->SetFont('helvetica', '', 11);

        // Company Header
        $pdf->SetFont('helvetica', 'B', 20);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(0, 15, 'PinkShield', 0, 1, 'L');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetTextColor(102, 102, 102);
        $pdf->Cell(0, 5, 'Medical & Consulting Services', 0, 1, 'L');
        
        // Invoice Info (right aligned)
        $pdf->SetY(15);
        $pdf->SetFont('helvetica', 'B', 14);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->Cell(0, 10, 'INVOICE', 0, 1, 'R');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->Cell(0, 5, 'Invoice #: ' . $invoiceNumber, 0, 1, 'R');
        $pdf->Cell(0, 5, 'Date: ' . $invoiceDate->format('M d, Y'), 0, 1, 'R');

        // Separator
        $pdf->SetDrawColor(196, 30, 58);
        $pdf->SetLineWidth(0.5);
        $pdf->Line(15, $pdf->GetY() + 3, 195, $pdf->GetY() + 3);
        $pdf->Ln(8);

        // Bill To / Service Provider - Fixed Layout
        $startBillToY = $pdf->GetY();
        
        // BILL TO column
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->SetX(15);
        $pdf->Cell(0, 5, 'BILL TO:', 0, 1, 'L');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetX(15);
        $pdf->MultiCell(90, 4, "Patient:\n" . $appointment->getPatientName() . "\n\nEmail:\n" . $appointment->getPatientEmail(), 0, 'L');
        
        // SERVICE PROVIDER column (positioned at original Y + offset)
        $pdf->SetY($startBillToY);
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->SetX(120);
        $pdf->Cell(0, 5, 'SERVICE PROVIDER:', 0, 1, 'R');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetX(120);
        $pdf->MultiCell(75, 4, "Doctor:\n" . $appointment->getDoctorName() . "\n\nEmail:\n" . $appointment->getDoctorEmail(), 0, 'R');

        $pdf->Ln(8);

        // Appointment Details
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(0, 7, 'APPOINTMENT DETAILS', 0, 1, 'L');
        
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->SetFillColor(245, 245, 245);
        $pdf->MultiCell(0, 5, 
            "Date & Time: " . $appointment->getAppointmentDate()->format('M d, Y H:i') . "\n" .
            "Status: " . ucfirst($appointment->getStatus()) . "\n" .
            "Notes: " . ($appointment->getNotes() ? $appointment->getNotes() : 'None'),
            1, 'L', TRUE
        );

        $pdf->Ln(5);

        // Products Table
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(0, 7, 'PARAPHARMACIE ITEMS', 0, 1, 'L');

        $pdf->Ln(2);

        // Table Header
        $pdf->SetFont('helvetica', 'B', 10);
        $pdf->SetTextColor(255, 255, 255);
        $pdf->SetFillColor(196, 30, 58);
        $pdf->Cell(80, 8, 'Product Name', 1, 0, 'L', TRUE);
        $pdf->Cell(50, 8, 'Description', 1, 0, 'L', TRUE);
        $pdf->Cell(35, 8, 'Price', 1, 1, 'R', TRUE);

        // Table Content
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->SetFillColor(255, 255, 255);

        if ($appointment->getParapharmacies() && count($appointment->getParapharmacies()) > 0) {
            foreach ($appointment->getParapharmacies() as $item) {
                $pdf->Cell(80, 7, substr($item->getName(), 0, 50), 1, 0, 'L');
                $pdf->Cell(50, 7, substr($item->getDescription() ?? '', 0, 30), 1, 0, 'L');
                $pdf->Cell(35, 7, '$' . number_format((float)$item->getPrice(), 2, '.', ','), 1, 1, 'R');
            }
        } else {
            $pdf->SetFillColor(240, 240, 240);
            $pdf->Cell(165, 7, 'No items', 1, 1, 'C', TRUE);
        }

        // Total
        $pdf->SetFont('helvetica', 'B', 12);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(130, 10, 'TOTAL AMOUNT:', 1, 0, 'R');
        $pdf->Cell(35, 10, '$' . number_format($total, 2, '.', ','), 1, 1, 'R');

        // Footer
        $pdf->Ln(10);
        $pdf->SetFont('helvetica', '', 9);
        $pdf->SetTextColor(102, 102, 102);
        $pdf->MultiCell(0, 4, 
            "Thank you for choosing PinkShield Medical Services.\n" .
            "This invoice is valid and officially issued for the appointment service provided.\n" .
            "Generated on " . date('M d, Y H:i'),
            0, 'C'
        );

        // Generate PDF content
        $pdfContent = $pdf->Output('', 'S'); // S = return as string

        return new Response(
            $pdfContent,
            Response::HTTP_OK,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="' . $invoiceNumber . '.pdf"'
            ]
        );
    }

    #[Route('/{id}/email-doctor', name: 'appointment_email_doctor', methods: ['POST'])]
    public function emailDoctor(Request $request, Appointment $appointment, AppointmentMailer $mailer): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // Only for completed appointments
        if ($appointment->getStatus() !== 'completed') {
            $this->addFlash('error', 'Email can only be sent for completed appointments.');
            return $this->redirectToRoute('appointment_show', ['id' => $appointment->getId()]);
        }

        // CSRF validation
        $token = $request->request->get('_csrf_token');
        if (!$this->isCsrfTokenValid('email_doctor' . $appointment->getId(), $token)) {
            $this->addFlash('error', 'Invalid CSRF token.');
            return $this->redirectToRoute('appointment_show', ['id' => $appointment->getId()]);
        }

        try {
            $mailer->sendAppointmentCompletedEmail($appointment);
            $this->addFlash('success', 'Email sent to the doctor.');
        } catch (\Throwable $e) {
            $this->addFlash('error', 'Failed to send email: ' . $e->getMessage());
        }

        return $this->redirectToRoute('appointment_show', ['id' => $appointment->getId()]);
    }

    private function calculateTotal(Appointment $appointment): float
    {
        $total = 0.0;
        
        if ($appointment->getParapharmacies()) {
            foreach ($appointment->getParapharmacies() as $item) {
                $price = $item->getPrice();
                if ($price !== null) {
                    $total += (float) $price;
                }
            }
        }
        
        return round($total, 2);
    }

    #[Route('/{id}/suggestion', name: 'appointment_suggestion')]
    public function suggestAppointment(DailyTrackingRepository $trackingRepository): Response
    {
        $stats = $trackingRepository->getStatistics($this->getUser());

        if ($stats['averageStress'] > 7 || $stats['averageMood'] < 4) {
            $suggestedDoctor = $stats['averageStress'] > 7 ? 'Therapist' : 'Psychologist';
            return $this->render('appointment/suggestion.html.twig', [
                'doctor' => $suggestedDoctor,
            ]);
        }

        return $this->render('appointment/suggestion.html.twig', [
            'doctor' => null,
        ]);
    }

    private function getEventColor(string $status): string
    {
        return match($status) {
            'confirmed' => '#28a745',
            'pending' => '#ffc107',
            default => '#007bff',
        };
    }

    private function getEventBorderColor(string $status): string
    {
        return match($status) {
            'confirmed' => '#1e7e34',
            'pending' => '#d39e00',
            default => '#0056b3',
        };
    }
}
