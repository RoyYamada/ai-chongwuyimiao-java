package com.example.demo.pet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Vaccination {
    private Long id;
    private Long petId;
    private Long vaccineId;
    private Integer doseNumber;
    private String administeredAt;
    private String lotNumber;
    private String clinic;
    private String vetName;
    private String nextDueAt;
    private String status;
    private String notes;
    private Boolean isUnvaccinated;

    private LocalDate nextDueDate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public String getAdministeredAt() {
        return administeredAt;
    }

    public void setAdministeredAt(String administeredAt) {
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

    public String getNextDueAt() {
        return nextDueAt;
    }

    public void setNextDueAt(String nextDueAt) {
        this.nextDueAt = nextDueAt;
        LocalDateTime dateTime = parseToLocalDateTime(nextDueAt);
        this.nextDueDate = (dateTime != null) ? dateTime.toLocalDate() : null;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
        if (nextDueDate != null) {
            this.nextDueAt = nextDueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        } else {
            this.nextDueAt = null;
        }
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

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public static String toInstantString(LocalDateTime ldt) {
        return ldt != null ? ldt.format(FORMATTER) : null;
    }

    public static LocalDateTime parseToLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, FORMATTER);
        } catch (Exception e1) {
            try {
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(dateStr, formatter2).atStartOfDay();
            } catch (Exception e2) {
                try {
                    DateTimeFormatter formatter3 = DateTimeFormatter.ISO_DATE_TIME;
                    return LocalDateTime.parse(dateStr, formatter3);
                } catch (Exception e3) {
                    try {
                        DateTimeFormatter formatter4 = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                        return ZonedDateTime.parse(dateStr, formatter4).toLocalDateTime();
                    } catch (Exception e4) {
                        try {
                            String modifiedStr = dateStr.replace(" ", "T");
                            return LocalDateTime.parse(modifiedStr, DateTimeFormatter.ISO_DATE_TIME);
                        } catch (Exception e5) {
                            try {
                                String modifiedStr = dateStr.replace(" ", "T").replace("+00", "+00:00");
                                return ZonedDateTime.parse(modifiedStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
                            } catch (Exception e6) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }
}