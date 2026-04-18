package com.example.demo.pet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class VaccinationReminder {
    private Long vaccinationId;
    private String petName;
    private String petBreed;
    private String vaccineName;
    private String doseInfo;
    private LocalDate dueDate;
    private long daysUntilDue;
    private boolean isOverdue;

    public VaccinationReminder(Long vaccinationId, String petName, String petBreed, String vaccineName, String doseInfo, String nextDueAt) {
        this.vaccinationId = vaccinationId;
        this.petName = petName;
        this.petBreed = petBreed;
        this.vaccineName = vaccineName;
        this.doseInfo = doseInfo;

        LocalDateTime dateTime = Vaccination.parseToLocalDateTime(nextDueAt);
        if (dateTime != null) {
            this.dueDate = dateTime.toLocalDate();
            LocalDate now = LocalDate.now();
            this.daysUntilDue = ChronoUnit.DAYS.between(now, this.dueDate);
            this.isOverdue = this.daysUntilDue < 0;
        } else {
            this.dueDate = null;
            this.daysUntilDue = 0;
            this.isOverdue = false;
        }
    }

    public VaccinationReminder(Long vaccinationId, String petName, String petBreed, String vaccineName, String doseInfo, LocalDate dueDate) {
        this.vaccinationId = vaccinationId;
        this.petName = petName;
        this.petBreed = petBreed;
        this.vaccineName = vaccineName;
        this.doseInfo = doseInfo;

        if (dueDate != null) {
            this.dueDate = dueDate;
            LocalDate now = LocalDate.now();
            this.daysUntilDue = ChronoUnit.DAYS.between(now, this.dueDate);
            this.isOverdue = this.daysUntilDue < 0;
        } else {
            this.dueDate = null;
            this.daysUntilDue = 0;
            this.isOverdue = false;
        }
    }

    public Long getVaccinationId() {
        return vaccinationId;
    }

    public void setVaccinationId(Long vaccinationId) {
        this.vaccinationId = vaccinationId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetBreed() {
        return petBreed;
    }

    public void setPetBreed(String petBreed) {
        this.petBreed = petBreed;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getDoseInfo() {
        return doseInfo;
    }

    public void setDoseInfo(String doseInfo) {
        this.doseInfo = doseInfo;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public long getDaysUntilDue() {
        return daysUntilDue;
    }

    public void setDaysUntilDue(long daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }

    @Override
    public String toString() {
        if (isOverdue) {
            return petName + " (" + petBreed + ") " + vaccineName + " " + doseInfo + " Due date: " + dueDate + " Overdue by: " + Math.abs(daysUntilDue) + " days";
        } else {
            return petName + " (" + petBreed + ") " + vaccineName + " " + doseInfo + " Due date: " + dueDate + " Days until: " + daysUntilDue + " days";
        }
    }
}