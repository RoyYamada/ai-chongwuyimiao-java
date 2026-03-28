package com.example.demo.pet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

    public VaccinationReminder(Long vaccinationId, String petName, String petBreed, String vaccineName, String doseInfo, Instant nextDueAt) {
        this.vaccinationId = vaccinationId;
        this.petName = petName;
        this.petBreed = petBreed;
        this.vaccineName = vaccineName;
        this.doseInfo = doseInfo;
        this.dueDate = nextDueAt.atZone(ZoneId.systemDefault()).toLocalDate();
        this.daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
        this.isOverdue = this.daysUntilDue < 0;
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
            return petName + "（" + petBreed + "）" + vaccineName + " " + doseInfo + "待接种日期：" + dueDate + "已超时：" + Math.abs(daysUntilDue) + " 天";
        } else {
            return petName + "（" + petBreed + "）" + vaccineName + " " + doseInfo + "待接种日期：" + dueDate + "距离接种还有：" + daysUntilDue + " 天";
        }
    }
}