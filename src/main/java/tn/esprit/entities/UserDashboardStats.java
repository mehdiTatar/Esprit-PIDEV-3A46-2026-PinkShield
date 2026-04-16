package tn.esprit.entities;

public class UserDashboardStats {
    private int adminCount;
    private int doctorCount;
    private int patientCount;

    public UserDashboardStats() {
    }

    public UserDashboardStats(int adminCount, int doctorCount, int patientCount) {
        this.adminCount = adminCount;
        this.doctorCount = doctorCount;
        this.patientCount = patientCount;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public int getDoctorCount() {
        return doctorCount;
    }

    public int getPatientCount() {
        return patientCount;
    }

    public int getTotalCount() {
        return adminCount + doctorCount + patientCount;
    }
}
