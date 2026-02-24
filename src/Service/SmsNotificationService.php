<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class SmsNotificationService
{
    private HttpClientInterface $httpClient;
    private string $twilioAccountSid;
    private string $twilioAuthToken;
    private string $twilioPhoneNumber;

    public function __construct(
        HttpClientInterface $httpClient,
        string $twilioAccountSid,
        string $twilioAuthToken,
        string $twilioPhoneNumber
    ) {
        $this->httpClient = $httpClient;
        $this->twilioAccountSid = $twilioAccountSid;
        $this->twilioAuthToken = $twilioAuthToken;
        $this->twilioPhoneNumber = $twilioPhoneNumber;
    }

    /**
     * Send appointment confirmation SMS
     *
     * @param string $patientPhoneNumber The patient's phone number (in E.164 format, e.g., +212612345678)
     * @param \DateTimeInterface $appointmentDate The appointment date and time
     * @param string $doctorName The doctor's name
     *
     * @return array The response from Twilio API
     * @throws \Exception If the SMS fails to send
     */
    public function sendAppointmentConfirmation(
        string $patientPhoneNumber,
        \DateTimeInterface $appointmentDate,
        string $doctorName
    ): array {
        $formattedDate = $appointmentDate->format('d/m/Y \a\t H:i');
        $message = "Your medical appointment with Dr. {$doctorName} is confirmed for {$formattedDate}. Please arrive 15 minutes early.";

        return $this->sendSms($patientPhoneNumber, $message);
    }

    /**
     * Send appointment completion SMS
     *
     * @param string $patientPhoneNumber The patient's phone number (in E.164 format, e.g., +212612345678)
     * @param string $doctorName The doctor's name
     *
     * @return array The response from Twilio API
     * @throws \Exception If the SMS fails to send
     */
    public function sendAppointmentCompletion(
        string $patientPhoneNumber,
        string $doctorName
    ): array {
        $message = "Thank you for visiting Dr. {$doctorName}. Your appointment has been completed. Take care!";

        return $this->sendSms($patientPhoneNumber, $message);
    }

    /**
     * Generic method to send SMS via Twilio
     *
     * @param string $toPhoneNumber Recipient's phone number (E.164 format)
     * @param string $messageBody The message body
     *
     * @return array The response from Twilio API
     * @throws \Exception If the SMS fails to send
     */
    private function sendSms(string $toPhoneNumber, string $messageBody): array
    {
        try {
            $url = "https://api.twilio.com/2010-04-01/Accounts/{$this->twilioAccountSid}/Messages.json";

            $response = $this->httpClient->request('POST', $url, [
                'auth_basic' => [$this->twilioAccountSid, $this->twilioAuthToken],
                'body' => [
                    'From' => $this->twilioPhoneNumber,
                    'To' => $toPhoneNumber,
                    'Body' => $messageBody,
                ],
            ]);

            $statusCode = $response->getStatusCode();

            if ($statusCode !== 201) {
                throw new \Exception("Twilio API returned status {$statusCode}");
            }

            return $response->toArray();
        } catch (\Exception $e) {
            // Log or handle the error as needed
            error_log("SMS sending failed: " . $e->getMessage());
            throw new \Exception("Failed to send SMS: " . $e->getMessage());
        }
    }
}
