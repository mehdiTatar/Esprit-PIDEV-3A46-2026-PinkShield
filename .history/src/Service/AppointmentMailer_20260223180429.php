<?php

namespace App\Service;

use App\Entity\Appointment;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Twig\Environment;
use TCPDF;
use Symfony\Component\Mime\Address;
use Symfony\Component\Mime\Part\DataPart;

class AppointmentMailer
{
    private MailerInterface $mailer;
    private Environment $twig;

    public function __construct(MailerInterface $mailer, Environment $twig)
    {
        $this->mailer = $mailer;
        $this->twig = $twig;
    }

    public function sendAppointmentCompletedEmail(Appointment $appointment): void
    {
        // generate PDF content (same layout as invoice)
        $pdfContent = $this->generateInvoicePdfContent($appointment);

        $email = (new TemplatedEmail())
            ->from(new Address('no-reply@pinkshield.com', 'PinkShield'))
            ->to(new Address($appointment->getDoctorEmail(), $appointment->getDoctorName() ?? $appointment->getDoctorEmail()))
            ->subject('Appointment Completed - Invoice Attached')
            ->htmlTemplate('emails/appointment_completed.html.twig')
            ->context([
                'appointment' => $appointment,
                'total' => $this->calculateTotal($appointment),
            ]);

        // attach PDF as data part
        $email->attach($pdfContent, 'invoice-' . $appointment->getId() . '.pdf', 'application/pdf');

        $this->mailer->send($email);
    }

    private function generateInvoicePdfContent(Appointment $appointment): string
    {
        $invoiceNumber = 'INV-' . $appointment->getId() . '-' . $appointment->getAppointmentDate()->format('Ymd');
        $invoiceDate = new \DateTime();
        $total = $this->calculateTotal($appointment);

        $pdf = new TCPDF();
        $pdf->SetCreator(PDF_CREATOR);
        $pdf->SetAuthor('PinkShield Medical Services');
        $pdf->SetTitle('Invoice ' . $invoiceNumber);
        $pdf->SetDefaultMonospacedFont(PDF_FONT_MONOSPACED);
        $pdf->SetMargins(15, 15, 15);
        $pdf->SetAutoPageBreak(TRUE, 15);
        $pdf->AddPage();
        $pdf->SetFont('helvetica', '', 11);

        // header
        $pdf->SetFont('helvetica', 'B', 20);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(0, 15, 'PinkShield', 0, 1, 'L');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetTextColor(102, 102, 102);
        $pdf->Cell(0, 5, 'Medical & Consulting Services', 0, 1, 'L');

        // invoice info on right
        $pdf->SetY(15);
        $pdf->SetFont('helvetica', 'B', 14);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->Cell(0, 10, 'INVOICE', 0, 1, 'R');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->Cell(0, 5, 'Invoice #: ' . $invoiceNumber, 0, 1, 'R');
        $pdf->Cell(0, 5, 'Date: ' . $invoiceDate->format('M d, Y'), 0, 1, 'R');

        // separator
        $pdf->SetDrawColor(196, 30, 58);
        $pdf->SetLineWidth(0.5);
        $pdf->Line(15, $pdf->GetY() + 3, 195, $pdf->GetY() + 3);
        $pdf->Ln(8);

        // Bill To / Service Provider - fixed layout
        $startBillToY = $pdf->GetY();
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->SetX(15);
        $pdf->Cell(0, 5, 'BILL TO:', 0, 1, 'L');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetX(15);
        $pdf->MultiCell(90, 4, "Patient:\n" . $appointment->getPatientName() . "\n\nEmail:\n" . $appointment->getPatientEmail(), 0, 'L');

        $pdf->SetY($startBillToY);
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(0, 0, 0);
        $pdf->SetX(120);
        $pdf->Cell(0, 5, 'SERVICE PROVIDER:', 0, 1, 'R');
        $pdf->SetFont('helvetica', '', 10);
        $pdf->SetX(120);
        $pdf->MultiCell(75, 4, "Doctor:\n" . $appointment->getDoctorName() . "\n\nEmail:\n" . $appointment->getDoctorEmail(), 0, 'R');

        $pdf->Ln(8);

        // appointment details
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

        // items table
        $pdf->SetFont('helvetica', 'B', 11);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(0, 7, 'PARAPHARMACIE ITEMS', 0, 1, 'L');
        $pdf->Ln(2);
        $pdf->SetFont('helvetica', 'B', 10);
        $pdf->SetTextColor(255, 255, 255);
        $pdf->SetFillColor(196, 30, 58);
        $pdf->Cell(80, 8, 'Product Name', 1, 0, 'L', TRUE);
        $pdf->Cell(50, 8, 'Description', 1, 0, 'L', TRUE);
        $pdf->Cell(35, 8, 'Price', 1, 1, 'R', TRUE);

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

        $pdf->SetFont('helvetica', 'B', 12);
        $pdf->SetTextColor(196, 30, 58);
        $pdf->Cell(130, 10, 'TOTAL AMOUNT:', 1, 0, 'R');
        $pdf->Cell(35, 10, '$' . number_format($total, 2, '.', ','), 1, 1, 'R');

        $pdf->Ln(10);
        $pdf->SetFont('helvetica', '', 9);
        $pdf->SetTextColor(102, 102, 102);
        $pdf->MultiCell(0, 4,
            "Thank you for choosing PinkShield Medical Services.\n" .
            "This invoice is valid and officially issued for the appointment service provided.\n" .
            "Generated on " . date('M d, Y H:i'),
            0, 'C'
        );

        return $pdf->Output('', 'S');
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
}
