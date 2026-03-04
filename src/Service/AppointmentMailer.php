<?php

namespace App\Service;

use App\Entity\Appointment;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

class AppointmentMailer
{
    public function __construct(
        private MailerInterface $mailer,
        private string $senderEmail
    ) {}

    public function sendAppointmentCompletedEmail(Appointment $appointment): void
    {
        $email = (new Email())
            ->from($this->senderEmail)
            ->to($appointment->getDoctorEmail())
            ->subject('Appointment Completed - ' . $appointment->getPatientName())
            ->html(sprintf(
                '<h2>Appointment Completed</h2>
                <p>The appointment with patient <strong>%s</strong> on <strong>%s</strong> has been marked as completed.</p>
                <p>Notes: %s</p>
                <br><p>— PinkShield Medical Services</p>',
                $appointment->getPatientName(),
                $appointment->getAppointmentDate()->format('M d, Y H:i'),
                $appointment->getNotes() ?: 'None'
            ));

        $this->mailer->send($email);
    }
}
