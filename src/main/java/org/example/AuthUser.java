package org.example;

public class AuthUser {
    private final int id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final String specialty;
    private final String medicalLicenseId;

    public AuthUser(String fullName, String email, String passwordHash) {
        this(0, fullName, email, passwordHash, "PATIENT", null, null);
    }

    public AuthUser(int id, String fullName, String email, String passwordHash, String role, String specialty, String medicalLicenseId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.specialty = specialty;
        this.medicalLicenseId = medicalLicenseId;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getMedicalLicenseId() {
        return medicalLicenseId;
    }
}

