package org.example;

import java.sql.*;
import java.util.ArrayList;

public class ServiceAppointment {

    private Connection cnx;

    public ServiceAppointment() {
        try {
            String url = "jdbc:mysql://localhost:3306/pinkshield_db";
            String user = "root";
            String password = "";

            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion a la base 'pinkshield_db' reussie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public void ajouter(Appointment appointment) throws SQLException {
        String req = "INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setString(1, appointment.getPatient_email());
        pst.setString(2, appointment.getPatient_name());
        pst.setString(3, appointment.getDoctor_email());
        pst.setString(4, appointment.getDoctor_name());
        pst.setTimestamp(5, appointment.getAppointment_date());
        pst.setString(6, appointment.getStatus());
        pst.setString(7, appointment.getNotes());

        pst.executeUpdate();
        System.out.println("Appointment ajoute avec succes !");
    }

    public void modifier(Appointment appointment) throws SQLException {
        String req = "UPDATE appointment SET patient_email = ?, patient_name = ?, doctor_email = ?, doctor_name = ?, appointment_date = ?, status = ?, notes = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setString(1, appointment.getPatient_email());
        pst.setString(2, appointment.getPatient_name());
        pst.setString(3, appointment.getDoctor_email());
        pst.setString(4, appointment.getDoctor_name());
        pst.setTimestamp(5, appointment.getAppointment_date());
        pst.setString(6, appointment.getStatus());
        pst.setString(7, appointment.getNotes());
        pst.setInt(8, appointment.getId());

        pst.executeUpdate();
        System.out.println("Appointment modifie avec succes !");
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM appointment WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setInt(1, id);

        pst.executeUpdate();
        System.out.println("Appointment supprime avec succes !");
    }

    public ArrayList<Appointment> afficherAll() throws SQLException {
        ArrayList<Appointment> list = new ArrayList<>();
        String req = "SELECT * FROM appointment";
        PreparedStatement pst = cnx.prepareStatement(req);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Appointment appointment = new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_email"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_email"),
                    rs.getString("doctor_name"),
                    rs.getTimestamp("appointment_date"),
                    rs.getString("status"),
                    rs.getString("notes"),
                    rs.getTimestamp("created_at")
            );
            list.add(appointment);
        }
        return list;
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
}

