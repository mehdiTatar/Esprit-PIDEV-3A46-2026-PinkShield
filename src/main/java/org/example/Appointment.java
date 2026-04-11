package org.example;

import java.sql.Timestamp;

public class Appointment {
    private int id;
    private String patient_email;
    private String patient_name;
    private String doctor_email;
    private String doctor_name;
    private Timestamp appointment_date;
    private String status;
    private String notes;
    private Timestamp created_at;

    public Appointment() {
    }

    public Appointment(String patient_email, String patient_name, String doctor_email, 
                      String doctor_name, Timestamp appointment_date, String status, String notes) {
        this.patient_email = patient_email;
        this.patient_name = patient_name;
        this.doctor_email = doctor_email;
        this.doctor_name = doctor_name;
        this.appointment_date = appointment_date;
        this.status = status;
        this.notes = notes;
    }

    public Appointment(int id, String patient_email, String patient_name, String doctor_email,
                      String doctor_name, Timestamp appointment_date, String status, String notes, Timestamp created_at) {
        this.id = id;
        this.patient_email = patient_email;
        this.patient_name = patient_name;
        this.doctor_email = doctor_email;
        this.doctor_name = doctor_name;
        this.appointment_date = appointment_date;
        this.status = status;
        this.notes = notes;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPatient_email() {
        return patient_email;
    }

    public void setPatient_email(String patient_email) {
        this.patient_email = patient_email;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getDoctor_email() {
        return doctor_email;
    }

    public void setDoctor_email(String doctor_email) {
        this.doctor_email = doctor_email;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public Timestamp getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(Timestamp appointment_date) {
        this.appointment_date = appointment_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient_email='" + patient_email + '\'' +
                ", patient_name='" + patient_name + '\'' +
                ", doctor_email='" + doctor_email + '\'' +
                ", doctor_name='" + doctor_name + '\'' +
                ", appointment_date=" + appointment_date +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}

