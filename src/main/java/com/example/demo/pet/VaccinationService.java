package com.example.demo.pet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VaccinationService {
    private final VaccineRepository vaccineRepository;
    private final VaccinationRepository vaccinationRepository;
    private final ReminderRepository reminderRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VaccinationService(VaccineRepository vaccineRepository, VaccinationRepository vaccinationRepository, ReminderRepository reminderRepository) {
        this.vaccineRepository = vaccineRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.reminderRepository = reminderRepository;
    }

    @Transactional
    public Long record(Vaccination v, Vaccine vaccineMeta) {
        String next = null;
        int dosesRequired = vaccineMeta.getDosesRequired() == null ? 1 : vaccineMeta.getDosesRequired();
        int intervalDays = vaccineMeta.getIntervalDays() == null ? 0 : vaccineMeta.getIntervalDays();
        int validMonths = vaccineMeta.getValidMonths() == null ? 12 : vaccineMeta.getValidMonths();
        LocalDateTime administered = Vaccination.parseToLocalDateTime(v.getAdministeredAt());
        Integer doseNumber = v.getDoseNumber() == null ? 0 : v.getDoseNumber();

        LocalDateTime baseTime = (administered == null) ? LocalDateTime.now(ZoneId.systemDefault()) : administered;
        if (doseNumber < dosesRequired) {
            LocalDateTime ldt = baseTime.plusDays(intervalDays);
            next = ldt.format(FORMATTER);
        } else {
            LocalDateTime ldt = baseTime.plusMonths(validMonths);
            next = ldt.format(FORMATTER);
        }

        v.setDoseNumber(doseNumber);
        if (v.getAdministeredAt() == null) {
            v.setAdministeredAt(LocalDateTime.now(ZoneId.systemDefault()).format(FORMATTER));
        }
        v.setNextDueAt(next);
        v.setStatus(v.getStatus() == null ? "PENDING" : v.getStatus());
        return vaccinationRepository.create(v);
    }

    @Transactional
    public VaccinationStatusUpdateResult updateVaccinationStatus(Long vaccinationId) {
        Vaccination currentVaccination = vaccinationRepository.findById(vaccinationId);
        if (currentVaccination == null) {
            throw new RuntimeException("接种记录不存在");
        }

        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(currentVaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("疫苗不存在"));

        vaccinationRepository.updateStatus(vaccinationId, "COMPLETED");

        String nextDueAt = null;
        int dosesRequired = vaccine.getDosesRequired() == null ? 1 : vaccine.getDosesRequired();
        int intervalDays = vaccine.getIntervalDays() == null ? 0 : vaccine.getIntervalDays();
        int validMonths = vaccine.getValidMonths() == null ? 12 : vaccine.getValidMonths();
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        Integer currentDoseNumber = currentVaccination.getDoseNumber() == null ? 0 : currentVaccination.getDoseNumber();
        if (currentDoseNumber < dosesRequired) {
            LocalDateTime ldt = now.plusDays(intervalDays);
            nextDueAt = ldt.format(FORMATTER);
        } else {
            LocalDateTime ldt = now.plusMonths(validMonths);
            nextDueAt = ldt.format(FORMATTER);
        }

        Vaccination nextVaccination = new Vaccination();
        nextVaccination.setPetId(currentVaccination.getPetId());
        nextVaccination.setVaccineId(currentVaccination.getVaccineId());
        nextVaccination.setDoseNumber(currentDoseNumber + 1);
        nextVaccination.setAdministeredAt(now.format(FORMATTER));
        nextVaccination.setStatus("PENDING");
        nextVaccination.setNextDueAt(nextDueAt);
        vaccinationRepository.create(nextVaccination);

        return new VaccinationStatusUpdateResult(true, nextDueAt);
    }

    @Transactional
    public Long createNextDoseAutomatically(Long currentVaccinationId) {
        Vaccination currentVaccination = vaccinationRepository.findById(currentVaccinationId);
        if (currentVaccination == null) {
            throw new RuntimeException("接种记录不存在");
        }

        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(currentVaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("疫苗不存在"));

        Vaccination nextVaccination = new Vaccination();
        nextVaccination.setPetId(currentVaccination.getPetId());
        nextVaccination.setVaccineId(currentVaccination.getVaccineId());
        Integer currentDoseNumber = currentVaccination.getDoseNumber() == null ? 0 : currentVaccination.getDoseNumber();
        nextVaccination.setDoseNumber(currentDoseNumber + 1);
        nextVaccination.setStatus("PENDING");

        String nextDueAt = null;
        int dosesRequired = vaccine.getDosesRequired() == null ? 1 : vaccine.getDosesRequired();
        int intervalDays = vaccine.getIntervalDays() == null ? 0 : vaccine.getIntervalDays();
        int validMonths = vaccine.getValidMonths() == null ? 12 : vaccine.getValidMonths();
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if (currentDoseNumber < dosesRequired) {
            LocalDateTime ldt = now.plusDays(intervalDays);
            nextDueAt = ldt.format(FORMATTER);
        } else {
            LocalDateTime ldt = now.plusMonths(validMonths);
            nextDueAt = ldt.format(FORMATTER);
        }

        nextVaccination.setNextDueAt(nextDueAt);

        return vaccinationRepository.create(nextVaccination);
    }

    @Transactional
    public Long createNextDoseManually(Long currentVaccinationId, String customNextDueAt, String clinic, String vetName) {
        Vaccination currentVaccination = vaccinationRepository.findById(currentVaccinationId);
        if (currentVaccination == null) {
            throw new RuntimeException("接种记录不存在");
        }

        Vaccination nextVaccination = new Vaccination();
        nextVaccination.setPetId(currentVaccination.getPetId());
        nextVaccination.setVaccineId(currentVaccination.getVaccineId());
        Integer currentDoseNumber = currentVaccination.getDoseNumber() == null ? 0 : currentVaccination.getDoseNumber();
        nextVaccination.setDoseNumber(currentDoseNumber + 1);
        nextVaccination.setStatus("PENDING");
        nextVaccination.setNextDueAt(customNextDueAt);
        nextVaccination.setClinic(clinic);
        nextVaccination.setVetName(vetName);
        return vaccinationRepository.create(nextVaccination);
    }

    public static class VaccinationStatusUpdateResult {
        private boolean success;
        private String nextDueAt;

        public VaccinationStatusUpdateResult(boolean success, String nextDueAt) {
            this.success = success;
            this.nextDueAt = nextDueAt;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getNextDueAt() {
            return nextDueAt;
        }

        public void setNextDueAt(String nextDueAt) {
            this.nextDueAt = nextDueAt;
        }
    }

    @Transactional
    public void deleteReminder(Long vaccinationId) {
        Vaccination vaccination;
        try {
            vaccination = vaccinationRepository.findById(vaccinationId);
        } catch (Exception e) {
            throw new RuntimeException("接种记录不存在");
        }

        vaccination.setNextDueAt(null);
        vaccinationRepository.update(vaccination);

        List<Reminder> reminders = reminderRepository.listByVaccination(vaccinationId);
        for (Reminder reminder : reminders) {
            reminderRepository.delete(reminder.getId());
        }
    }
}