package org.example;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServiceUser {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/pinkshield_db?serverVersion=8.0&charset=utf8mb4";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private final Connection cnx;

    public ServiceUser() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Connexion user à la base réussie !");
            ensureTableExists(connection);
        } catch (SQLException e) {
            System.out.println("❌ Erreur de connexion user : " + e.getMessage());
        }
        this.cnx = connection;
    }

    public boolean register(String fullName, String email, String plainPassword) {
        return registerWithRole(fullName, email, plainPassword, "PATIENT");
    }

    public boolean registerWithRole(String fullName, String email, String plainPassword, String role) {
        if (cnx == null) {
            return false;
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || userExists(normalizedEmail)) {
            return false;
        }

        String normalizedRole = role == null ? "PATIENT" : role.trim().toUpperCase();
        if (!"ADMIN".equals(normalizedRole)) {
            normalizedRole = "PATIENT";
        }

        String sql = "INSERT INTO app_users (full_name, email, password_hash, role, specialty, medical_license_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, fullName.trim());
            pst.setString(2, normalizedEmail);
            pst.setString(3, hashPassword(plainPassword));
            pst.setString(4, normalizedRole);
            pst.setString(5, null);
            pst.setString(6, null);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur register user : " + e.getMessage());
            return false;
        }
    }

    public boolean registerAdmin(String fullName, String email, String specialty, String medicalLicenseId, String plainPassword) {
        if (cnx == null) {
            return false;
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || userExists(normalizedEmail)) {
            return false;
        }

        String sql = "INSERT INTO app_users (full_name, email, password_hash, role, specialty, medical_license_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, fullName.trim());
            pst.setString(2, normalizedEmail);
            pst.setString(3, hashPassword(plainPassword));
            pst.setString(4, "ADMIN");
            pst.setString(5, specialty == null ? null : specialty.trim());
            pst.setString(6, medicalLicenseId == null ? null : medicalLicenseId.trim());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur register admin : " + e.getMessage());
            return false;
        }
    }

    public boolean authenticate(String email, String plainPassword) {
        return authenticateUser(email, plainPassword) != null;
    }

    public AuthUser authenticateUser(String email, String plainPassword) {
        if (cnx == null) {
            return null;
        }

        String sql = "SELECT id, full_name, email, password_hash, role, specialty, medical_license_id FROM app_users WHERE email = ? LIMIT 1";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, normalizeEmail(email));
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) {
                return null;
            }

            String storedHash = rs.getString("password_hash");
            if (!verifyPassword(plainPassword, storedHash)) {
                return null;
            }

            return new AuthUser(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    storedHash,
                    rs.getString("role"),
                    rs.getString("specialty"),
                    rs.getString("medical_license_id")
            );
        } catch (SQLException e) {
            System.out.println("❌ Erreur authenticate user : " + e.getMessage());
            return null;
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean userExists(String normalizedEmail) {
        String sql = "SELECT 1 FROM app_users WHERE email = ? LIMIT 1";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, normalizedEmail);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("❌ Erreur check user : " + e.getMessage());
            return false;
        }
    }

    private void ensureTableExists(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS app_users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "full_name VARCHAR(150) NOT NULL, " +
                "email VARCHAR(190) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL DEFAULT 'PATIENT', " +
                "specialty VARCHAR(120) NULL, " +
                "medical_license_id VARCHAR(80) NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.executeUpdate();
        }
    }

    private static String hashPassword(String password) {
        try {
            Class<?> bcryptClass = Class.forName("org.mindrot.jbcrypt.BCrypt");
            Object salt = bcryptClass.getMethod("gensalt", int.class).invoke(null, 12);
            return (String) bcryptClass.getMethod("hashpw", String.class, String.class)
                    .invoke(null, password, salt);
        } catch (Exception ignored) {
            return "sha256$" + sha256(password);
        }
    }

    private static boolean verifyPassword(String rawPassword, String storedHash) {
        try {
            Class<?> bcryptClass = Class.forName("org.mindrot.jbcrypt.BCrypt");
            if (!storedHash.startsWith("$2")) {
                return "sha256$".concat(sha256(rawPassword)).equals(storedHash);
            }
            return (boolean) bcryptClass.getMethod("checkpw", String.class, String.class)
                    .invoke(null, rawPassword, storedHash);
        } catch (Exception ignored) {
            return "sha256$".concat(sha256(rawPassword)).equals(storedHash);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}

