package tn.esprit.entities;

import java.time.LocalDate;

public class DailyTrackingTrendPoint {
    private LocalDate date;
    private Double averageMood;
    private Double averageStress;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAverageMood() {
        return averageMood;
    }

    public void setAverageMood(Double averageMood) {
        this.averageMood = averageMood;
    }

    public Double getAverageStress() {
        return averageStress;
    }

    public void setAverageStress(Double averageStress) {
        this.averageStress = averageStress;
    }
}
