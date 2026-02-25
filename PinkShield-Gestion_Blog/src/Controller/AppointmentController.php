<?php

namespace App\Controller;

use App\Entity\Appointment;
use App\Entity\User;
use App\Entity\Doctor;
use App\Repository\AppointmentRepository;
use App\Repository\DoctorRepository;
use App\Repository\DailyTrackingRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use App\Form\ParapharmacieType;
use App\Form\AppointmentFormType;
use App\Entity\Parapharmacie;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/appointment')]
class AppointmentController extends AbstractController
{
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

    #[Route('/new', name: 'appointment_new')]
    public function new(Request $request, DoctorRepository $doctorRepository, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        // Only patients can book appointments
        if ($this->isGranted('ROLE_DOCTOR') || $this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Only patients can book appointments.');
            return $this->redirectToRoute('appointment_index');
        }

        $appointment = new Appointment();
        $appointment->setPatientEmail($this->getUser()->getUserIdentifier());
        $appointment->setPatientName($this->getUser() instanceof User ? $this->getUser()->getFullName() : 'Patient');
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

            $this->addFlash('success', 'Appointment booked successfully! Waiting for doctor confirmation.');
            return $this->redirectToRoute('appointment_index');
        }

        $doctors = $doctorRepository->findAll();

        return $this->render('appointment/new.html.twig', [
            'form' => $form->createView(),
            'doctors' => $doctors,
        ]);
    }

    #[Route('/{id}/confirm', name: 'appointment_confirm')]
    #[IsGranted('ROLE_DOCTOR')]
    public function confirm(Appointment $appointment, EntityManagerInterface $entityManager): Response
    {
        if ($appointment->getDoctorEmail() !== $this->getUser()->getUserIdentifier() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException();
        }

        $appointment->setStatus('confirmed');
        $entityManager->flush();

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
    public function edit(Appointment $appointment, Request $request, EntityManagerInterface $entityManager): Response
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
            if ($dateStr) {
                $appointment->setAppointmentDate(new \DateTime($dateStr));
            }
            $appointment->setNotes($notes);
            if ($status) {
                $appointment->setStatus($status);
            }
            $entityManager->flush();
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

    #[Route('/suggestion', name: 'appointment_suggestion')]
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
}
