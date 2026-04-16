package com.example.demo.pet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class VaccinationService {
    private final VaccineRepository vaccineRepository;
    private final VaccinationRepository vaccinationRepository;
    private final ReminderRepository reminderRepository;

    public VaccinationService(VaccineRepository vaccineRepository, VaccinationRepository vaccinationRepository, ReminderRepository reminderRepository) {
        this.vaccineRepository = vaccineRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.reminderRepository = reminderRepository;
    }

    @Transactional
    public Long record(Vaccination v, Vaccine vaccineMeta) {
        Instant next = null;
        int dosesRequired = vaccineMeta.getDosesRequired() == null ? 1 : vaccineMeta.getDosesRequired();
        int intervalDays = vaccineMeta.getIntervalDays() == null ? 0 : vaccineMeta.getIntervalDays();
        int validMonths = vaccineMeta.getValidMonths() == null ? 12 : vaccineMeta.getValidMonths();
        Instant administered = v.getAdministeredAt() == null ? null : v.getAdministeredAt();
        Integer doseNumber = v.getDoseNumber() == null ? 0 : v.getDoseNumber();
        if (doseNumber < dosesRequired) {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered == null ? Instant.now() : administered, ZoneId.systemDefault()).plusDays(intervalDays);
            next = ldt.atZone(ZoneId.systemDefault()).toInstant();
        } else {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered == null ? Instant.now() : administered, ZoneId.systemDefault()).plusMonths(validMonths);
            next = ldt.atZone(ZoneId.systemDefault()).toInstant();
        }
        v.setDoseNumber(doseNumber);
        v.setAdministeredAt(administered);
        v.setNextDueAt(next);
        v.setStatus(v.getStatus() == null ? "PENDING" : v.getStatus());
        return vaccinationRepository.create(v);
    }

    @Transactional
    public VaccinationStatusUpdateResult updateVaccinationStatus(Long vaccinationId) {
        // 查找当前接种记录
        Vaccination currentVaccination = vaccinationRepository.findById(vaccinationId);
        if (currentVaccination == null) {
            throw new RuntimeException("接种记录不存在");
        }

        // 获取疫苗信息
        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(currentVaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("疫苗不存在"));

        // 更新状态为已接种
        vaccinationRepository.updateStatus(vaccinationId, "COMPLETED");

        // 计算下次接种日期
        Instant nextDueAt = null;
        int dosesRequired = vaccine.getDosesRequired() == null ? 1 : vaccine.getDosesRequired();
        int intervalDays = vaccine.getIntervalDays() == null ? 0 : vaccine.getIntervalDays();
        int validMonths = vaccine.getValidMonths() == null ? 12 : vaccine.getValidMonths();
        // 使用当前时间作为计算基准，确保下一针时间是未来的
        Instant administered = Instant.now();

        Integer currentDoseNumber = currentVaccination.getDoseNumber() == null ? 0 : currentVaccination.getDoseNumber();
        if (currentDoseNumber < dosesRequired) {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered, ZoneId.systemDefault()).plusDays(intervalDays);
            nextDueAt = ldt.atZone(ZoneId.systemDefault()).toInstant();
        } else {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered, ZoneId.systemDefault()).plusMonths(validMonths);
            nextDueAt = ldt.atZone(ZoneId.systemDefault()).toInstant();
        }

        // 自动创建下一针记录
        Vaccination nextVaccination = new Vaccination();
        nextVaccination.setPetId(currentVaccination.getPetId());
        nextVaccination.setVaccineId(currentVaccination.getVaccineId());
        nextVaccination.setDoseNumber(currentDoseNumber + 1);
        nextVaccination.setAdministeredAt(Instant.now()); // 将当前时间作为实际接种时间
        nextVaccination.setStatus("PENDING");
        nextVaccination.setNextDueAt(nextDueAt);
        vaccinationRepository.create(nextVaccination);

        return new VaccinationStatusUpdateResult(true, nextDueAt);
    }

    @Transactional
    public Long createNextDoseAutomatically(Long currentVaccinationId) {
        // 查找当前接种记录
        Vaccination currentVaccination = vaccinationRepository.findById(currentVaccinationId);
        if (currentVaccination == null) {
            throw new RuntimeException("接种记录不存在");
        }

        // 获取疫苗信息
        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(currentVaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("疫苗不存在"));

        // 创建下一针记录
        Vaccination nextVaccination = new Vaccination();
        nextVaccination.setPetId(currentVaccination.getPetId());
        nextVaccination.setVaccineId(currentVaccination.getVaccineId());
        Integer currentDoseNumber = currentVaccination.getDoseNumber() == null ? 0 : currentVaccination.getDoseNumber();
        nextVaccination.setDoseNumber(currentDoseNumber + 1);
        nextVaccination.setStatus("PENDING");

        // 计算下次接种日期
        Instant nextDueAt = null;
        int dosesRequired = vaccine.getDosesRequired() == null ? 1 : vaccine.getDosesRequired();
        int intervalDays = vaccine.getIntervalDays() == null ? 0 : vaccine.getIntervalDays();
        int validMonths = vaccine.getValidMonths() == null ? 12 : vaccine.getValidMonths();
        // 使用当前时间作为计算基准，确保下一针时间是未来的
        Instant administered = Instant.now();

        if (currentDoseNumber < dosesRequired) {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered, ZoneId.systemDefault()).plusDays(intervalDays);
            nextDueAt = ldt.atZone(ZoneId.systemDefault()).toInstant();
        } else {
            LocalDateTime ldt = LocalDateTime.ofInstant(administered, ZoneId.systemDefault()).plusMonths(validMonths);
            nextDueAt = ldt.atZone(ZoneId.systemDefault()).toInstant();
        }

        nextVaccination.setNextDueAt(nextDueAt);

        // 保存下一针记录
        return vaccinationRepository.create(nextVaccination);
    }

    @Transactional
    public Long createNextDoseManually(Long currentVaccinationId, Instant customNextDueAt, String clinic, String vetName) {
        // 查找当前接种记录
        Vaccination currentVaccination = vaccinationRepository.findById(currentVaccinationId);
        if (currentVaccination == null) {
            throw new RuntimeException("接种记录不存在");
        }

        // 创建下一针记录
        Vaccination nextVaccination = new Vaccination();
        nextVaccination.setPetId(currentVaccination.getPetId());
        nextVaccination.setVaccineId(currentVaccination.getVaccineId());
        Integer currentDoseNumber = currentVaccination.getDoseNumber() == null ? 0 : currentVaccination.getDoseNumber();
        nextVaccination.setDoseNumber(currentDoseNumber + 1);
        nextVaccination.setStatus("PENDING");
        nextVaccination.setNextDueAt(customNextDueAt);
        nextVaccination.setClinic(clinic);
        nextVaccination.setVetName(vetName);
        // 保存下一针记录
        return vaccinationRepository.create(nextVaccination);
    }

    // 用于返回状态更新结果的内部类
    public static class VaccinationStatusUpdateResult {
        private boolean success;
        private Instant nextDueAt;

        public VaccinationStatusUpdateResult(boolean success, Instant nextDueAt) {
            this.success = success;
            this.nextDueAt = nextDueAt;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Instant getNextDueAt() {
            return nextDueAt;
        }

        public void setNextDueAt(Instant nextDueAt) {
            this.nextDueAt = nextDueAt;
        }
    }

    @Transactional
    public void deleteReminder(Long vaccinationId) {
        // 查找接种记录
        Vaccination vaccination;
        try {
            vaccination = vaccinationRepository.findById(vaccinationId);
        } catch (Exception e) {
            throw new RuntimeException("接种记录不存在");
        }

        // 将 next_due_at 置空
        vaccination.setNextDueAt(null);
        // 更新接种记录
        vaccinationRepository.update(vaccination);

        // 删除相关的提醒记录
        List<Reminder> reminders = reminderRepository.listByVaccination(vaccinationId);
        for (Reminder reminder : reminders) {
            reminderRepository.delete(reminder.getId());
        }
    }
}
