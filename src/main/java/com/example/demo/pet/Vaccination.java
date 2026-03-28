package com.example.demo.pet;

import java.time.Instant;

public class Vaccination {
    private Long id;
    private Long petId;
    private Long vaccineId;
    private Integer doseNumber;
    private Instant administeredAt;
    private String lotNumber;
    private String clinic;
    private String vetName;
    private Instant nextDueAt;
    private String status;
    private String notes;
    private Boolean isUnvaccinated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }

    public Integer getDoseNumber() {
        return doseNumber;
    }

    public void setDoseNumber(Integer doseNumber) {
        this.doseNumber = doseNumber;
    }

    public Instant getAdministeredAt() {
        return administeredAt;
    }

    public void setAdministeredAt(Instant administeredAt) {
        this.administeredAt = administeredAt;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getClinic() {
        return clinic;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    public Instant getNextDueAt() {
        return nextDueAt;
    }

    public void setNextDueAt(Instant nextDueAt) {
        this.nextDueAt = nextDueAt;
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

    public Boolean getIsUnvaccinated() {
        return isUnvaccinated;
    }

    public void setIsUnvaccinated(Boolean isUnvaccinated) {
        this.isUnvaccinated = isUnvaccinated;
    }
}
