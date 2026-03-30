package com.example.demo.pet;

import java.time.Instant;
import java.time.LocalDate;

public class Reminder {
    private Long id;
    private String templateId;
    private Long vaccinationId;
    private String openid;
    private LocalDate reminderDate;
    private String reminderThing;
    private String location;
    private String targetName;
    private String notes;
    private Boolean sent;
    private Instant sentAt;
    private String sendError;
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

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
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

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public String getSendError() {
        return sendError;
    }

    public void setSendError(String sendError) {
        this.sendError = sendError;
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