<?php

namespace App\Service;

use App\Entity\Appointment;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

class AppointmentMailer
{
<<<<<<< HEAD
    public function __construct(
        private MailerInterface $mailer,
        private string $senderEmail
    ) {}
=======
    private MailerInterface $mailer;

    public function __construct(MailerInterface $mailer)
    {
        $this->mailer = $mailer;
    }
>>>>>>> f6cc000b0612f83d55ba4325b4872374266fe173

    public function sendAppointmentCompletedEmail(Appointment $appointment): void
    {
        $email = (new Email())
<<<<<<< HEAD
            ->from($this->senderEmail)
=======
            ->from('noreply@pinkshield.com')
>>>>>>> f6cc000b0612f83d55ba4325b4872374266fe173
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
