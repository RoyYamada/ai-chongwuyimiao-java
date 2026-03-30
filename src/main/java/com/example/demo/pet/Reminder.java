package com.example.demo.pet;

import java.time.LocalDate;
import java.time.Instant;

public class Reminder {
    private Long id;
    private String templateId;
    private Long vaccinationId;
    private LocalDate reminderDate;
    private String reminderThing;
    private String location;
    private String targetName;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Long getVaccinationId() {
        return vaccinationId;
    }

    public void setVaccinationId(Long vaccinationId) {
        this.vaccinationId = vaccinationId;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderThing() {
        return reminderThing;
    }

    public void setReminderThing(String reminderThing) {
        this.reminderThing = reminderThing;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}