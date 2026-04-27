package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AppointmentPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PDFSHIFT_ENDPOINT = "https://api.pdfshift.io/v3/convert/pdf";

    private static final String DEFAULT_PUBLIC_RECORDS_BASE_URL = "http://localhost/pinkshield/storage";

    public static final class CloudPdfResult {
        private final byte[] pdfBytes;
        private final String publicUrl;
        private final boolean generatedByCloudApi;

        public CloudPdfResult(byte[] pdfBytes, String publicUrl, boolean generatedByCloudApi) {
            this.pdfBytes = pdfBytes;
            this.publicUrl = publicUrl;
            this.generatedByCloudApi = generatedByCloudApi;
        }

        public byte[] getPdfBytes() {
            return pdfBytes;
        }

        public String getPublicUrl() {
            return publicUrl;
        }

        public boolean isGeneratedByCloudApi() {
            return generatedByCloudApi;
        }
    }

    public void exportAppointmentProof(Appointment appointment, File outputFile) throws IOException {
        String appointmentDate = appointment.getAppointment_date() != null
                ? appointment.getAppointment_date().toLocalDateTime().format(DATE_TIME_FORMATTER)
                : "N/A";

        List<String> lines = new ArrayList<>();
        lines.add("PinkShield - Appointment Proof");
        lines.add(" ");
        lines.add("Appointment Details");
        lines.add("Appointment ID: " + appointment.getId());
        lines.add("Patient Name: " + safe(appointment.getPatient_name()));
        lines.add("Patient Email: " + safe(appointment.getPatient_email()));
        lines.add("Doctor Name: " + safe(appointment.getDoctor_name()));
        lines.add("Appointment Date: " + appointmentDate);
        lines.add("Status: " + safe(appointment.getStatus()));
        lines.add("Notes: " + safe(appointment.getNotes()));
        lines.add(" ");
        lines.add("This PDF is generated as patient appointment proof.");

        String contentStream = buildTextContentStream(lines);
        byte[] proofPdf = buildMinimalPdf(contentStream);
        Files.write(outputFile.toPath(), proofPdf);
    }

    public CloudPdfResult exportAppointmentInvoiceWithCloudLink(Appointment appointment, File outputFile) throws IOException {
        if (outputFile == null) {
            throw new FileNotFoundException("No destination file selected.");
        }

        CloudPdfResult result = generateCloudPDF(appointment);
        Files.write(outputFile.toPath(), result.getPdfBytes());
        return result;
    }

    public void exportAppointmentInvoice(Appointment appointment, File outputFile) throws IOException {
        exportAppointmentInvoiceWithCloudLink(appointment, outputFile);
    }

    public CloudPdfResult generateCloudPDF(Appointment appointment) {
        byte[] pdfBytes;
        boolean generatedByCloudApi = false;

        String apiKey = getPdfShiftApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String html = getInvoiceHtmlTemplate(appointment);
                pdfBytes = generateBeautifulPdf(html, apiKey);
                generatedByCloudApi = true;
            } catch (IOException cloudFailure) {
                // Keep invoice export available even if the cloud API is temporarily unavailable.
                pdfBytes = buildLegacyInvoicePdfBytes(appointment);
            }
        } else {
            // Missing API key: use local fallback but keep stable demo URL generation for QR flow.
            pdfBytes = buildLegacyInvoicePdfBytes(appointment);
        }

        String publicUrl = uploadPdfAndGetPublicUrl(appointment, pdfBytes);
        return new CloudPdfResult(pdfBytes, publicUrl, generatedByCloudApi);
    }

    public String getPublicPdfUrl(Appointment appointment) {
        String baseUrl = System.getProperty("pinkshield.public.records.baseurl");
        if (baseUrl == null || baseUrl.isBlank()) {
            String envBase = System.getenv("PINKSHIELD_PUBLIC_RECORDS_BASE_URL");
            baseUrl = (envBase == null || envBase.isBlank()) ? DEFAULT_PUBLIC_RECORDS_BASE_URL : envBase;
        }

        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + "/INV-" + appointment.getId() + ".pdf";
    }

    private String uploadPdfAndGetPublicUrl(Appointment appointment, byte[] pdfBytes) {
        try {
            File mirrorDir = new File("target/cloud-records");
            if (!mirrorDir.exists() && !mirrorDir.mkdirs()) {
                return getPublicPdfUrl(appointment);
            }
            File mirroredPdf = new File(mirrorDir, "INV-" + appointment.getId() + ".pdf");
            Files.write(mirroredPdf.toPath(), pdfBytes);
        } catch (IOException ignored) {
            // Demo upload mirroring should not block patient workflow.
        }

        return getPublicPdfUrl(appointment);
    }

    private String getPdfShiftApiKey() {
        String envKey = System.getenv("PDFSHIFT_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        String propertyKey = System.getProperty("pdfshift.api.key");
        return propertyKey == null ? null : propertyKey.trim();
    }

    /**
     * Task 1: The Modern HTML/CSS Template
     * Returns a complete, modern HTML5 document with embedded CSS
     * suitable for professional invoice generation.
     *
     * @param appointment the appointment to render
     * @return HTML string with embedded CSS
     */
    public String getInvoiceHtmlTemplate(Appointment appointment) {
        String appointmentDate = appointment.getAppointment_date() != null
                ? appointment.getAppointment_date().toLocalDateTime().format(DATE_TIME_FORMATTER)
                : "N/A";
        String invoiceDate = LocalDate.now().format(INVOICE_DATE_FORMAT);
        String generatedAt = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        String patientColor = "#e84393";
        String doctorColor = "#0984e3";
        String invoiceRef = "INV-" + appointment.getId() + "-" + invoiceDate;

        return "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'/>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>"
                + "<title>PinkShield Invoice - " + htmlEscape(invoiceRef) + "</title>"
                + getInvoiceCss(patientColor, doctorColor)
                + "</head>"
                + "<body>"
                + "<div class='sheet'>"
                + getInvoiceHeader()
                + "<div class='body'>"
                + getInvoiceMetaSection(appointment, patientColor, doctorColor)
                + getInvoiceTable(invoiceRef, appointmentDate, appointment.getStatus(), generatedAt)
                + getInvoiceNotes(appointment)
                + getInvoiceFooter(patientColor, doctorColor)
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    /**
     * Returns the embedded CSS styling for the invoice.
     */
    private String getInvoiceCss(String patientColor, String doctorColor) {
        return "<style>"
                + "* { margin: 0; padding: 0; box-sizing: border-box; }"
                + "body { "
                + "  font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; "
                + "  background: linear-gradient(135deg, #f6f7fb 0%, #eef2f7 100%); "
                + "  color: #2d3436; "
                + "  padding: 26px 12px; "
                + "  line-height: 1.6; "
                + "}"
                + ".sheet { "
                + "  max-width: 900px; "
                + "  margin: 0 auto; "
                + "  background: #ffffff; "
                + "  border: 1px solid #e7e8ed; "
                + "  border-radius: 14px; "
                + "  overflow: hidden; "
                + "  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); "
                + "}"
                + ".header { "
                + "  background: linear-gradient(135deg, " + patientColor + " 0%, #d4357e 100%); "
                + "  color: #fff; "
                + "  padding: 28px 32px; "
                + "  display: flex; "
                + "  justify-content: space-between; "
                + "  align-items: center; "
                + "  border-bottom: 2px solid rgba(255, 255, 255, 0.1); "
                + "}"
                + ".logo { "
                + "  font-size: 28px; "
                + "  font-weight: 700; "
                + "  letter-spacing: 0.3px; "
                + "  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); "
                + "}"
                + ".invoiceTag { "
                + "  font-size: 12px; "
                + "  font-weight: 700; "
                + "  letter-spacing: 0.15em; "
                + "  background: rgba(255, 255, 255, 0.2); "
                + "  padding: 6px 12px; "
                + "  border-radius: 999px; "
                + "}"
                + ".body { padding: 32px; }"
                + ".meta { "
                + "  display: grid; "
                + "  grid-template-columns: 1fr 1fr; "
                + "  gap: 24px; "
                + "  margin-bottom: 32px; "
                + "}"
                + "@media (max-width: 600px) { "
                + "  .meta { grid-template-columns: 1fr; } "
                + "}"
                + ".panel { "
                + "  border: 1px solid #e9edf3; "
                + "  border-radius: 10px; "
                + "  padding: 18px; "
                + "  background: #fff; "
                + "  transition: all 0.3s ease; "
                + "}"
                + ".panel:hover { "
                + "  border-color: #d4d8e0; "
                + "  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); "
                + "}"
                + ".panel h4 { "
                + "  margin: 0 0 12px 0; "
                + "  font-size: 12px; "
                + "  text-transform: uppercase; "
                + "  letter-spacing: 0.1em; "
                + "  font-weight: 700; "
                + "}"
                + ".bill h4 { color: " + patientColor + "; }"
                + ".provider h4 { color: " + doctorColor + "; }"
                + ".line { "
                + "  font-size: 14px; "
                + "  line-height: 1.7; "
                + "  color: #2d3436; "
                + "}"
                + ".line strong { "
                + "  color: #1e272e; "
                + "  display: block; "
                + "  margin-bottom: 4px; "
                + "}"
                + "table { "
                + "  width: 100%; "
                + "  border-collapse: separate; "
                + "  border-spacing: 0; "
                + "  font-size: 13px; "
                + "  margin-top: 8px; "
                + "  border-radius: 8px; "
                + "  overflow: hidden; "
                + "  margin-bottom: 24px; "
                + "}"
                + "th, td { "
                + "  border: 1px solid #e9edf3; "
                + "  padding: 12px 14px; "
                + "  text-align: left; "
                + "}"
                + "th { "
                + "  background: linear-gradient(135deg, #f8f9fc 0%, #f1f2f6 100%); "
                + "  font-weight: 700; "
                + "  color: #1e272e; "
                + "  border-bottom: 2px solid #e9edf3; "
                + "}"
                + "td { background: #fff; }"
                + "tr:last-child td { border-bottom: 2px solid #e9edf3; }"
                + ".status { "
                + "  display: inline-block; "
                + "  padding: 5px 12px; "
                + "  border-radius: 999px; "
                + "  background: #ffeaf4; "
                + "  color: " + patientColor + "; "
                + "  font-weight: 700; "
                + "  font-size: 12px; "
                + "  text-transform: capitalize; "
                + "}"
                + ".notes { "
                + "  margin-top: 20px; "
                + "  border: 1px solid #ffd9eb; "
                + "  background: #fff8fc; "
                + "  border-radius: 10px; "
                + "  padding: 16px; "
                + "  font-size: 13px; "
                + "  white-space: pre-wrap; "
                + "  word-wrap: break-word; "
                + "  color: #2d3436; "
                + "}"
                + ".notes strong { "
                + "  color: " + patientColor + "; "
                + "  display: block; "
                + "  margin-bottom: 8px; "
                + "  font-size: 14px; "
                + "}"
                + ".footer { "
                + "  margin-top: 24px; "
                + "  padding-top: 16px; "
                + "  border-top: 1px solid #e9edf3; "
                + "  color: #636e72; "
                + "  font-size: 12px; "
                + "  text-align: center; "
                + "}"
                + "</style>";
    }

    private String getInvoiceHeader() {
        return "<div class='header'>"
                + "<div class='logo'>🏥 PinkShield</div>"
                + "<div class='invoiceTag'>PROFESSIONAL INVOICE</div>"
                + "</div>";
    }

    private String getInvoiceMetaSection(Appointment appointment, String patientColor, String doctorColor) {
        return "<div class='meta'>"
                + "<div class='panel bill'>"
                + "<h4>💳 Bill To</h4>"
                + "<div class='line'>"
                + "<strong>" + htmlEscape(safe(appointment.getPatient_name(), "Patient")) + "</strong>"
                + htmlEscape(safe(appointment.getPatient_email()))
                + "</div>"
                + "</div>"
                + "<div class='panel provider'>"
                + "<h4>👨‍⚕️ Service Provider</h4>"
                + "<div class='line'>"
                + "<strong>" + htmlEscape(safe(appointment.getDoctor_name())) + "</strong>"
                + htmlEscape(inferDoctorSpecialty(appointment))
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String getInvoiceTable(String invoiceRef, String appointmentDate, String status, String generatedAt) {
        return "<table>"
                + "<thead><tr>"
                + "<th>Invoice #</th>"
                + "<th>Appointment Date</th>"
                + "<th>Status</th>"
                + "<th>Generated</th>"
                + "</tr></thead>"
                + "<tbody><tr>"
                + "<td>" + htmlEscape(invoiceRef) + "</td>"
                + "<td>" + htmlEscape(appointmentDate) + "</td>"
                + "<td><span class='status'>" + htmlEscape(safe(status)) + "</span></td>"
                + "<td>" + htmlEscape(generatedAt) + "</td>"
                + "</tr></tbody>"
                + "</table>";
    }

    private String getInvoiceNotes(Appointment appointment) {
        return "<div class='notes'>"
                + "<strong>📋 Medical Notes</strong>"
                + htmlEscape(safe(appointment.getNotes(), "No additional notes provided."))
                + "</div>";
    }

    private String getInvoiceFooter(String patientColor, String doctorColor) {
        return "<div class='footer'>"
                + "This is an official PinkShield Medical Services document. "
                + "Patient Portal: " + patientColor + " | Doctor Portal: " + doctorColor
                + "</div>";
    }

    /**
     * Task 2: The HTML-to-PDF API Integration
     * Generates a beautiful PDF using the PDFShift API with modern HTML/CSS.
     * Uses Java's built-in java.net.http.HttpClient for HTTP requests.
     *
     * @param htmlContent the HTML content to convert
     * @param apiKey the PDFShift API key
     * @return byte array containing the generated PDF
     * @throws IOException if the API request fails
     */
    private byte[] generateBeautifulPdf(String htmlContent, String apiKey) throws IOException {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            String payload = "{"
                    + "\"source\":\"" + jsonEscape(htmlContent) + "\","
                    + "\"sandbox\":false,"
                    + "\"use_print\":false,"
                    + "\"landscape\":false"
                    + "}";

            String authRaw = apiKey + ":";
            String auth = Base64.getEncoder().encodeToString(authRaw.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PDFSHIFT_ENDPOINT))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Authorization", "Basic " + auth)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(response.body(), StandardCharsets.UTF_8);
                throw new IOException("PDFShift API error (" + response.statusCode() + "): " + errorBody);
            }

            if (response.body().length == 0) {
                throw new IOException("PDFShift returned an empty PDF response.");
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PDF generation interrupted: " + e.getMessage(), e);
        }
    }

    private byte[] buildLegacyInvoicePdfBytes(Appointment appointment) {
        String appointmentDate = appointment.getAppointment_date() != null
                ? appointment.getAppointment_date().toLocalDateTime().format(DATE_TIME_FORMATTER)
                : "N/A";
        String invoiceDate = LocalDate.now().format(INVOICE_DATE_FORMAT);
        String generatedAt = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        List<String> lines = new ArrayList<>();
        lines.add("PinkShield");
        lines.add("Medical & Consulting Services");
        lines.add("INVOICE");
        lines.add(" ");
        lines.add("BILL TO:");
        lines.add("Patient: " + safe(appointment.getPatient_name()));
        lines.add("Email: " + safe(appointment.getPatient_email()));
        lines.add(" ");
        lines.add("SERVICE PROVIDER:");
        lines.add("Doctor: " + safe(appointment.getDoctor_name()));
        lines.add("Email: doctor@pinkshield.com");
        lines.add(" ");
        lines.add("Invoice #: INV-" + appointment.getId() + "-" + invoiceDate);
        lines.add("APPOINTMENT DETAILS");
        lines.add("Date & Time: " + appointmentDate);
        lines.add("Status: " + safe(appointment.getStatus()));
        lines.add("Notes: " + safe(appointment.getNotes()));
        lines.add(" ");
        lines.add("PARAPHARMACIE ITEMS");
        lines.add("Product Name | Description | Price");
        lines.add("No items     | No items    | TND 0.00");
        lines.add("TOTAL AMOUNT: TND 0.00");
        lines.add(" ");
        lines.add("Thank you for choosing PinkShield Medical Services.");
        lines.add("Generated on " + generatedAt);

        String contentStream = buildTextContentStream(lines);
        return buildMinimalPdf(contentStream);
    }

    private byte[] buildMinimalPdf(String contentStream) {
        StringBuilder pdf = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();

        pdf.append("%PDF-1.4\n");

        offsets.add(pdf.length());
        pdf.append("1 0 obj\n")
                .append("<< /Type /Catalog /Pages 2 0 R >>\n")
                .append("endobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj\n")
                .append("<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n")
                .append("endobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj\n")
                .append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\n")
                .append("endobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj\n")
                .append("<< /Length ").append(contentStream.getBytes(StandardCharsets.US_ASCII).length).append(" >>\n")
                .append("stream\n")
                .append(contentStream)
                .append("\nendstream\n")
                .append("endobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj\n")
                .append("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\n")
                .append("endobj\n");

        int xrefStart = pdf.length();
        pdf.append("xref\n")
                .append("0 6\n")
                .append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }

        pdf.append("trailer\n")
                .append("<< /Size 6 /Root 1 0 R >>\n")
                .append("startxref\n")
                .append(xrefStart)
                .append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private String inferDoctorSpecialty(Appointment appointment) {
        String doctorName = safe(appointment.getDoctor_name()).toLowerCase();
        String notes = safe(appointment.getNotes()).toLowerCase();

        if (doctorName.contains("cardio") || notes.contains("heart") || notes.contains("chest pain")) {
            return "Cardiology";
        }
        if (doctorName.contains("derm") || notes.contains("skin") || notes.contains("rash")) {
            return "Dermatology";
        }
        if (doctorName.contains("neuro") || notes.contains("headache") || notes.contains("migraine")) {
            return "Neurology";
        }
        if (doctorName.contains("pedia") || notes.contains("child") || notes.contains("baby")) {
            return "Pediatrics";
        }
        if (doctorName.contains("gyn") || notes.contains("pregnan")) {
            return "Gynecology";
        }

        return "General Medicine";
    }

    private String safe(String value) {
        return safe(value, "N/A");
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String buildTextContentStream(List<String> lines) {
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n")
                .append("/F1 12 Tf\n")
                .append("50 790 Td\n");

        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                stream.append("0 -18 Td\n");
            }
            stream.append("(")
                    .append(pdfEscape(toAscii(lines.get(i))))
                    .append(") Tj\n");
        }

        stream.append("ET");
        return stream.toString();
    }

    private String pdfEscape(String value) {
        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String toAscii(String value) {
        StringBuilder ascii = new StringBuilder();
        for (char ch : value.toCharArray()) {
            ascii.append(ch <= 127 ? ch : '?');
        }
        return ascii.toString();
    }

    private String htmlEscape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
