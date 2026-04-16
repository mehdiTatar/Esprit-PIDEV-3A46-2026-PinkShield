package tn.esprit.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyTrackingEntry {
    private int id;
    private int userId;
    private String userName;
    private LocalDate date;
    private Integer mood;
    private Integer stress;
    private String activities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer anxietyLevel;
    private Integer focusLevel;
    private Integer motivationLevel;
    private Integer socialInteractionLevel;
    private Integer sleepHours;
    private Integer energyLevel;
    private String symptoms;
    private Boolean medicationTaken;
    private Integer appetiteLevel;
    private Integer waterIntake;
    private Integer physicalActivityLevel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }

    public Integer getStress() {
        return stress;
    }

    public void setStress(Integer stress) {
        this.stress = stress;
    }

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getAnxietyLevel() {
        return anxietyLevel;
    }

    public void setAnxietyLevel(Integer anxietyLevel) {
        this.anxietyLevel = anxietyLevel;
    }

    public Integer getFocusLevel() {
        return focusLevel;
    }

    public void setFocusLevel(Integer focusLevel) {
        this.focusLevel = focusLevel;
    }

    public Integer getMotivationLevel() {
        return motivationLevel;
    }

    public void setMotivationLevel(Integer motivationLevel) {
        this.motivationLevel = motivationLevel;
    }

    public Integer getSocialInteractionLevel() {
        return socialInteractionLevel;
    }

    public void setSocialInteractionLevel(Integer socialInteractionLevel) {
        this.socialInteractionLevel = socialInteractionLevel;
    }

    public Integer getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(Integer sleepHours) {
        this.sleepHours = sleepHours;
    }

    public Integer getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(Integer energyLevel) {
        this.energyLevel = energyLevel;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Boolean getMedicationTaken() {
        return medicationTaken;
    }

    public void setMedicationTaken(Boolean medicationTaken) {
        this.medicationTaken = medicationTaken;
    }

    public Integer getAppetiteLevel() {
        return appetiteLevel;
    }

    public void setAppetiteLevel(Integer appetiteLevel) {
        this.appetiteLevel = appetiteLevel;
    }

    public Integer getWaterIntake() {
        return waterIntake;
    }

    public void setWaterIntake(Integer waterIntake) {
        this.waterIntake = waterIntake;
    }

    public Integer getPhysicalActivityLevel() {
        return physicalActivityLevel;
    }

    public void setPhysicalActivityLevel(Integer physicalActivityLevel) {
        this.physicalActivityLevel = physicalActivityLevel;
    }
}
