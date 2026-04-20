package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        Files.writeString(outputFile.toPath(), pdf.toString(), StandardCharsets.US_ASCII);
    }

    public void exportAppointmentInvoice(Appointment appointment, File outputFile) throws IOException {
        if (outputFile == null) {
            throw new FileNotFoundException("No destination file selected.");
        }

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
        lines.add("No items     | No items    | $0.00");
        lines.add("TOTAL AMOUNT: $0.00");
        lines.add(" ");
        lines.add("Thank you for choosing PinkShield Medical Services.");
        lines.add("Generated on " + generatedAt);

        String contentStream = buildTextContentStream(lines);
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

        Files.writeString(outputFile.toPath(), pdf.toString(), StandardCharsets.US_ASCII);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
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
}

