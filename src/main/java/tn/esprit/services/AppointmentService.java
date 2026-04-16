package tn.esprit.services;

import tn.esprit.entities.Appointment;
import tn.esprit.utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private final Connection conn;

    public AppointmentService() {
        conn = MyDB.getInstance().getConnection();
    }

    public boolean addAppointment(Appointment appt) {
        String query = "INSERT INTO appointment (patient_id, doctor_id, patient_name, doctor_name, appointment_date, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setString(3, appt.getPatientName());
            ps.setString(4, appt.getDoctorName());
            ps.setTimestamp(5, appt.getAppointmentDate());
            ps.setString(6, appt.getStatus());
            ps.setString(7, appt.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT * FROM appointment ORDER BY appointment_date ASC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return getAppointmentsByForeignKey("patient_id", patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return getAppointmentsByForeignKey("doctor_id", doctorId);
    }

    public int countAppointmentsByPatient(int patientId) {
        return countAppointments("patient_id", patientId, false);
    }

    public int countAppointmentsByDoctor(int doctorId) {
        return countAppointments("doctor_id", doctorId, false);
    }

    public int countUpcomingAppointmentsByPatient(int patientId) {
        return countAppointments("patient_id", patientId, true);
    }

    public int countUpcomingAppointmentsByDoctor(int doctorId) {
        return countAppointments("doctor_id", doctorId, true);
    }

    public boolean updateStatus(int id, String status) {
        String query = "UPDATE appointment SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAppointment(int id) {
        String query = "DELETE FROM appointment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAppointmentConflict(int doctorId, java.sql.Timestamp appointmentDate, Integer excludeAppointmentId) {
        String query = "SELECT COUNT(*) FROM appointment WHERE doctor_id = ? AND appointment_date = ? AND id <> ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setTimestamp(2, appointmentDate);
            ps.setInt(3, excludeAppointmentId == null ? -1 : excludeAppointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasPatientDuplicate(int patientId, int doctorId, java.sql.Timestamp appointmentDate, Integer excludeAppointmentId) {
        String query = "SELECT COUNT(*) FROM appointment WHERE patient_id = ? AND doctor_id = ? AND appointment_date = ? AND id <> ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setTimestamp(3, appointmentDate);
            ps.setInt(4, excludeAppointmentId == null ? -1 : excludeAppointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Appointment> getAppointmentsByForeignKey(String column, int value) {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT * FROM appointment WHERE " + column + " = ? ORDER BY appointment_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private int countAppointments(String column, int value, boolean upcomingOnly) {
        String query = upcomingOnly
                ? "SELECT COUNT(*) FROM appointment WHERE " + column + " = ? AND appointment_date >= ?"
                : "SELECT COUNT(*) FROM appointment WHERE " + column + " = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, value);
            if (upcomingOnly) {
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("patient_name"),
                rs.getString("doctor_name"),
                rs.getTimestamp("appointment_date"),
                rs.getString("status"),
                rs.getString("notes")
        );
    }
}
