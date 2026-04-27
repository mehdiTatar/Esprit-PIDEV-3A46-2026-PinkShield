package org.example;

import java.sql.*;
import java.util.ArrayList;

public class ServiceAppointment {

    private Connection cnx;
    private boolean hasAppointmentUserIdColumn;

    public ServiceAppointment() {
        try {
            String url = "jdbc:mysql://localhost:3306/pinkshield_db";
            String user = "root";
            String password = "";

            cnx = DriverManager.getConnection(url, user, password);
            hasAppointmentUserIdColumn = hasColumn("appointment", "user_id");
            System.out.println("Connexion a la base 'pinkshield_db' reussie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public void ajouter(Appointment appointment) throws SQLException {
        UserSession session = UserSession.getInstance();
        String patientEmail = session.isLoggedIn() ? session.getEmail() : appointment.getPatient_email();
        String patientName = session.isLoggedIn() ? session.getName() : appointment.getPatient_name();

        String req;
        if (hasAppointmentUserIdColumn) {
            req = "INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            req = "INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, patientEmail);
            pst.setString(2, patientName);
            pst.setString(3, appointment.getDoctor_email());
            pst.setString(4, appointment.getDoctor_name());
            pst.setTimestamp(5, appointment.getAppointment_date());
            pst.setString(6, appointment.getStatus());
            pst.setString(7, appointment.getNotes());
            if (hasAppointmentUserIdColumn) {
                if (session.isLoggedIn()) {
                    pst.setInt(8, session.getUserId());
                } else {
                    pst.setNull(8, Types.INTEGER);
                }
            }
            pst.executeUpdate();
        }
        System.out.println("✅ Appointment ajoute avec succes !");
    }

    public void modifier(Appointment appointment) throws SQLException {
        UserSession session = UserSession.getInstance();
        boolean scopeToCurrentUser = session.isLoggedIn() && !session.isAdmin();
        String req = "UPDATE appointment SET patient_email = ?, patient_name = ?, doctor_email = ?, doctor_name = ?, appointment_date = ?, status = ?, notes = ? WHERE id = ?"
                + (scopeToCurrentUser ? " AND patient_email = ?" : "");

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, scopeToCurrentUser ? session.getEmail() : appointment.getPatient_email());
            pst.setString(2, scopeToCurrentUser ? session.getName() : appointment.getPatient_name());
            pst.setString(3, appointment.getDoctor_email());
            pst.setString(4, appointment.getDoctor_name());
            pst.setTimestamp(5, appointment.getAppointment_date());
            pst.setString(6, appointment.getStatus());
            pst.setString(7, appointment.getNotes());
            pst.setInt(8, appointment.getId());
            if (scopeToCurrentUser) {
                pst.setString(9, session.getEmail());
            }

            pst.executeUpdate();
        }
        System.out.println("✅ Appointment modifie avec succes !");
    }

    public void supprimer(int id) throws SQLException {
        UserSession session = UserSession.getInstance();
        boolean scopeToCurrentUser = session.isLoggedIn() && !session.isAdmin();
        String req = "DELETE FROM appointment WHERE id = ?" + (scopeToCurrentUser ? " AND patient_email = ?" : "");

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            if (scopeToCurrentUser) {
                pst.setString(2, session.getEmail());
            }
            pst.executeUpdate();
        }
        System.out.println("✅ Appointment supprime avec succes !");
    }

    public ArrayList<Appointment> afficherAll() throws SQLException {
        String req = "SELECT * FROM appointment ORDER BY appointment_date DESC";
        try (PreparedStatement pst = cnx.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            return mapAppointments(rs);
        }
    }

    public ArrayList<Appointment> getByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            return new ArrayList<>();
        }

        if (hasAppointmentUserIdColumn) {
            String req = "SELECT * FROM appointment WHERE user_id = ? ORDER BY appointment_date DESC";
            try (PreparedStatement pst = cnx.prepareStatement(req)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    return mapAppointments(rs);
                }
            }
        }

        String patientEmail = findUserEmailById(userId);
        if (patientEmail.isBlank()) {
            return new ArrayList<>();
        }

        String req = "SELECT * FROM appointment WHERE LOWER(patient_email) = LOWER(?) ORDER BY appointment_date DESC";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, patientEmail);
            try (ResultSet rs = pst.executeQuery()) {
                return mapAppointments(rs);
            }
        }
    }

    public boolean isDoctorBooked(String doctorEmail, Timestamp appointmentDate) throws SQLException {
        String req = "SELECT COUNT(*) as count FROM appointment WHERE doctor_email = ? AND appointment_date = ? AND status != 'cancelled'";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, doctorEmail);
        pst.setTimestamp(2, appointmentDate);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
        return false;
    }

    private ArrayList<Appointment> mapAppointments(ResultSet rs) throws SQLException {
        ArrayList<Appointment> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_email"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_email"),
                    rs.getString("doctor_name"),
                    rs.getTimestamp("appointment_date"),
                    rs.getString("status"),
                    rs.getString("notes"),
                    rs.getTimestamp("created_at")
            ));
        }
        return list;
    }

    private String findUserEmailById(int userId) throws SQLException {
        String req = "SELECT email FROM app_users WHERE id = ? LIMIT 1";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    return email == null ? "" : email.trim();
                }
            }
        }
        return "";
    }

    private boolean hasColumn(String tableName, String columnName) {
        if (cnx == null) {
            return false;
        }

        String req = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, tableName);
            pst.setString(2, columnName);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {
            return false;
        }
    }
}