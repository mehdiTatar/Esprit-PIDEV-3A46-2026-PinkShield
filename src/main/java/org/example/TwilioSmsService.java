package org.example;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * TwilioSmsService: SMS notification service for PinkShield medical appointments
 * 
 * This service integrates Twilio API to send appointment confirmations via SMS.
 * Designed specifically for Tunisia (+216 country code).
 * 
 * Configuration:
 * - Set environment variables or system properties for ACCOUNT_SID and AUTH_TOKEN
 * - TWILIO_ACCOUNT_SID
 * - TWILIO_AUTH_TOKEN
 * - TWILIO_PHONE_NUMBER (your Twilio sender phone)
 * 
 * Usage:
 *   TwilioSmsService sms = new TwilioSmsService();
 *   sms.sendAppointmentConfirmation("+21698765432", "Fady Ahmed", "2026-04-28 14:30");
 */
public class TwilioSmsService {

    private final String accountSid;
    private final String authToken;
    private final String fromPhoneNumber;

    /**
     * Constructor: Initialize Twilio SMS service
     * Reads credentials from environment variables or system properties
     */
    public TwilioSmsService() {
        // Read Twilio credentials from environment variables
        this.accountSid = getConfigValue("TWILIO_ACCOUNT_SID", "twilio.account.sid");
        this.authToken = getConfigValue("TWILIO_AUTH_TOKEN", "twilio.auth.token");
        this.fromPhoneNumber = getConfigValue("TWILIO_PHONE_NUMBER", "twilio.phone.number");

        // Initialize Twilio SDK only if credentials are available
        if (isConfigured()) {
            Twilio.init(this.accountSid, this.authToken);
            System.out.println("✅ Twilio SMS Service Initialized");
        } else {
            System.out.println("⚠️ Twilio SMS Service: Credentials not configured. SMS notifications will be disabled.");
        }
    }

    /**
     * Send SMS appointment confirmation to patient
     * 
     * @param patientPhone Phone number in format: +21698765432 (Tunisia)
     * @param patientName  Patient's full name
     * @param appointmentDate Appointment date and time
     */
    public void sendAppointmentConfirmation(String patientPhone, String patientName, String appointmentDate) {
        if (!isConfigured()) {
            System.out.println("⚠️ SMS Service not configured. Skipping SMS notification.");
            return;
        }

        try {
            // Validate phone number format
            if (!isValidTunisianPhone(patientPhone)) {
                System.out.println("❌ Invalid Tunisian phone number: " + patientPhone);
                return;
            }

            // Build the SMS message body
            String messageBody = buildAppointmentSmsBody(patientName, appointmentDate);

            // Send SMS via Twilio
            Message message = Message.creator(
                    new PhoneNumber(patientPhone),      // To number (recipient)
                    new PhoneNumber(fromPhoneNumber),   // From number (sender - Twilio)
                    messageBody                         // SMS body
            ).create();

            System.out.println("✅ SMS Sent Successfully!");
            System.out.println("   Message SID: " + message.getSid());
            System.out.println("   Status: " + message.getStatus());
            System.out.println("   To: " + patientPhone);

        } catch (Exception e) {
            System.err.println("❌ Error sending SMS: " + e.getMessage());
            e.printStackTrace();
            // Do NOT crash the app - just log the error
        }
    }

    /**
     * Send appointment reminder SMS (24 hours before)
     * 
     * @param patientPhone Phone number in format: +21698765432
     * @param patientName  Patient's name
     * @param appointmentDate Appointment date and time
     */
    public void sendAppointmentReminder(String patientPhone, String patientName, String appointmentDate) {
        if (!isConfigured()) {
            return;
        }

        try {
            if (!isValidTunisianPhone(patientPhone)) {
                System.out.println("❌ Invalid Tunisian phone number: " + patientPhone);
                return;
            }

            String messageBody = buildAppointmentReminderSmsBody(patientName, appointmentDate);

            Message message = Message.creator(
                    new PhoneNumber(patientPhone),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            System.out.println("✅ Reminder SMS Sent Successfully!");
            System.out.println("   Message SID: " + message.getSid());
            System.out.println("   To: " + patientPhone);

        } catch (Exception e) {
            System.err.println("❌ Error sending reminder SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send appointment cancellation SMS
     * 
     * @param patientPhone Phone number
     * @param patientName  Patient's name
     */
    public void sendAppointmentCancellation(String patientPhone, String patientName) {
        if (!isConfigured()) {
            return;
        }

        try {
            if (!isValidTunisianPhone(patientPhone)) {
                System.out.println("❌ Invalid Tunisian phone number: " + patientPhone);
                return;
            }

            String messageBody = buildCancellationSmsBody(patientName);

            Message message = Message.creator(
                    new PhoneNumber(patientPhone),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            System.out.println("✅ Cancellation SMS Sent Successfully!");
            System.out.println("   Message SID: " + message.getSid());
            System.out.println("   To: " + patientPhone);

        } catch (Exception e) {
            System.err.println("❌ Error sending cancellation SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build the appointment confirmation SMS message
     */
    private String buildAppointmentSmsBody(String patientName, String appointmentDate) {
        return "PinkShield: Hello " + patientName + ", your medical appointment on " + appointmentDate 
               + " is confirmed. Have a great day!";
    }

    /**
     * Build the appointment reminder SMS message
     */
    private String buildAppointmentReminderSmsBody(String patientName, String appointmentDate) {
        return "PinkShield Reminder: Hi " + patientName + ", your appointment is tomorrow at " + appointmentDate 
               + ". See you soon!";
    }

    /**
     * Build the cancellation SMS message
     */
    private String buildCancellationSmsBody(String patientName) {
        return "PinkShield: Hello " + patientName + ", your appointment has been cancelled. "
               + "Please contact us to reschedule.";
    }

    /**
     * Validate Tunisian phone number format
     * Valid formats:
     * - +21698765432 (with +216 country code)
     * - +216 98 765 432 (with spaces)
     * - 98765432 (without country code, we'll add it)
     */
    private boolean isValidTunisianPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        // Remove spaces and hyphens for validation
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");

        // Check if it's a valid Tunisian number
        // Tunisia country code: +216
        // Valid patterns:
        // +21698765432 (13 chars with +216)
        // 21698765432 (12 chars with 216)
        // 98765432 (8 chars, local format)

        if (cleaned.startsWith("+216") && cleaned.length() == 13) {
            return true; // Valid: +21698765432
        } else if (cleaned.startsWith("216") && cleaned.length() == 12) {
            return true; // Valid: 21698765432
        } else if (!cleaned.startsWith("0") && cleaned.length() == 8) {
            return true; // Valid: 98765432 (will add +216)
        }

        return false;
    }

    /**
     * Normalize Tunisian phone number to international format
     * Converts all valid formats to +21698765432
     */
    public String normalizeTunisianPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }

        String cleaned = phoneNumber.replaceAll("[\\s-]", "");

        // Already in +216 format
        if (cleaned.startsWith("+216")) {
            return cleaned;
        }

        // In 216 format without +
        if (cleaned.startsWith("216")) {
            return "+" + cleaned;
        }

        // Local format: just the 8 digits
        if (cleaned.length() == 8) {
            return "+216" + cleaned;
        }

        // If it starts with 0, remove it and add +216
        if (cleaned.startsWith("0")) {
            return "+216" + cleaned.substring(1);
        }

        return cleaned;
    }

    /**
     * Check if SMS service is properly configured
     */
    private boolean isConfigured() {
        return accountSid != null && !accountSid.isEmpty() &&
               authToken != null && !authToken.isEmpty() &&
               fromPhoneNumber != null && !fromPhoneNumber.isEmpty();
    }

    /**
     * Get configuration value from environment variables or system properties
     */
    private String getConfigValue(String envVarName, String systemPropertyName) {
        // Try environment variable first
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Try system property
        String propValue = System.getProperty(systemPropertyName);
        if (propValue != null && !propValue.isEmpty()) {
            return propValue;
        }

        return null;
    }

    /**
     * Test method to verify SMS configuration
     * Usage: TwilioSmsService sms = new TwilioSmsService(); sms.testConfiguration();
     */
    public void testConfiguration() {
        System.out.println("\n🧪 Twilio SMS Service Configuration Test");
        System.out.println("========================================");
        System.out.println("Account SID: " + (accountSid != null ? "✅ Configured" : "❌ Missing"));
        System.out.println("Auth Token: " + (authToken != null ? "✅ Configured" : "❌ Missing"));
        System.out.println("Phone Number: " + (fromPhoneNumber != null ? fromPhoneNumber + " ✅" : "❌ Missing"));
        System.out.println("Service Status: " + (isConfigured() ? "🟢 READY" : "🔴 NOT READY"));
        System.out.println("========================================\n");
    }
}

