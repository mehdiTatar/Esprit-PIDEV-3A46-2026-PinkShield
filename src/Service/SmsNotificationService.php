<?php

namespace App\Service;

class SmsNotificationService
{
    public function sendAppointmentConfirmation(string $phone, \DateTimeInterface $appointmentDate, string $doctorName): void
    {
        // SMS sending stub - integrate with Twilio/Vonage etc. when ready
        error_log(sprintf(
            'SMS: Appointment confirmed with Dr. %s on %s. Sent to %s',
            $doctorName,
            $appointmentDate->format('Y-m-d H:i'),
            $phone
        ));
    }

    public function sendAppointmentCompletion(string $phone, string $doctorName): void
    {
        error_log(sprintf(
            'SMS: Your appointment with Dr. %s has been completed. Sent to %s',
            $doctorName,
            $phone
        ));
    }
}
