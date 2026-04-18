package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@Tag(name = "提醒", description = "疫苗接种提醒相关接口")
public class ReminderController {
    private final ReminderRepository repo;
    private final VaccinationRepository vaccinationRepository;
    private final PetRepository petRepository;
    private final VaccineRepository vaccineRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReminderController(ReminderRepository repo, VaccinationRepository vaccinationRepository, PetRepository petRepository, VaccineRepository vaccineRepository) {
        this.repo = repo;
        this.vaccinationRepository = vaccinationRepository;
        this.petRepository = petRepository;
        this.vaccineRepository = vaccineRepository;
    }

    @PostMapping
    @Operation(summary = "创建提醒", description = "创建新的疫苗接种提醒")
    public Long create(@RequestBody Reminder r) {
        return repo.create(r);
    }

    @PostMapping("/create-from-vaccination")
    @Operation(summary = "根据疫苗接种记录创建提醒", description = "根据疫苗接种记录创建提醒，只需要提供template_id、vaccination_id和openid")
    public Long createFromVaccination(@RequestParam String templateId, @RequestParam Long vaccinationId, @RequestParam String openid) {
        Vaccination vaccination = vaccinationRepository.findById(vaccinationId);
        if (vaccination == null) {
            throw new RuntimeException("疫苗接种记录不存在");
        }

        Pet pet = petRepository.findById(vaccination.getPetId())
                .orElseThrow(() -> new RuntimeException("宠物不存在"));

        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(vaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("疫苗不存在"));

        Reminder reminder = new Reminder();
        reminder.setTemplateId(templateId);
        reminder.setVaccinationId(vaccinationId);
        reminder.setOpenid(openid);

        LocalDate nextDueDate = Vaccination.parseToLocalDateTime(vaccination.getNextDueAt()).toLocalDate();
        reminder.setReminderDate(nextDueDate);

        String petInfo = pet.getName() + "(" + pet.getBreed() + ")";
        String doseInfo;
        Integer doseNumber = vaccination.getDoseNumber() == null ? 0 : vaccination.getDoseNumber();
        if (doseNumber < vaccine.getDosesRequired()) {
            doseInfo = "第 " + (doseNumber + 1) + " 针";
        } else {
            doseInfo = "加强针";
        }

        reminder.setReminderThing(petInfo + "-" + vaccine.getName() + doseInfo);
        reminder.setLocation(vaccination.getClinic());
        reminder.setTargetName(petInfo);
        reminder.setNotes(vaccination.getNotes());

        return repo.create(reminder);
    }

    @PutMapping("/{id}/update-date")
    @Operation(summary = "修改提醒时间", description = "修改提醒的时间，并同步更新疫苗接种记录的 nextDueAt 字段")
    public void updateReminderDate(@PathVariable Long id, @RequestParam String newDate) {
        Reminder reminder = repo.findById(id);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }

        LocalDate newReminderDate = parseDate(newDate);
        reminder.setReminderDate(newReminderDate);
        repo.update(reminder);

        Long vaccinationId = reminder.getVaccinationId();
        if (vaccinationId != null) {
            Vaccination vaccination = vaccinationRepository.findById(vaccinationId);
            if (vaccination != null) {
                LocalDateTime newDueDateTime = newReminderDate.atStartOfDay();
                String newNextDueAt = newDueDateTime.format(FORMATTER);
                vaccination.setNextDueAt(newNextDueAt);
                vaccinationRepository.update(vaccination);
            }
        }
    }

    @PutMapping("/by-vaccination/{vaccinationId}/update-date")
    @Operation(summary = "通过疫苗接种ID修改提醒时间", description = "通过疫苗接种ID修改提醒的时间，并同步更新疫苗接种记录的 nextDueAt 字段")
    public void updateReminderDateByVaccinationId(@PathVariable Long vaccinationId, @RequestParam String newDate) {
        List<Reminder> reminders = repo.listByVaccination(vaccinationId);
        if (reminders.isEmpty()) {
            throw new RuntimeException("该疫苗接种记录没有相关的提醒");
        }

        LocalDate newReminderDate = parseDate(newDate);
        for (Reminder reminder : reminders) {
            reminder.setReminderDate(newReminderDate);
            repo.update(reminder);
        }

        Vaccination vaccination = vaccinationRepository.findById(vaccinationId);
        if (vaccination != null) {
            LocalDateTime newDueDateTime = newReminderDate.atStartOfDay();
            String newNextDueAt = newDueDateTime.format(FORMATTER);
            vaccination.setNextDueAt(newNextDueAt);
            vaccinationRepository.update(vaccination);
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, FORMATTER);
                return dateTime.toLocalDate();
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(dateStr);
                } catch (DateTimeParseException e3) {
                    throw new RuntimeException("日期格式不正确，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss 格式");
                }
            }
        }
    }

    @GetMapping("/by-vaccination/{vaccinationId}")
    @Operation(summary = "获取疫苗接种的提醒", description = "根据疫苗接种ID获取相关的提醒")
    public List<Reminder> byVaccination(@PathVariable Long vaccinationId) {
        return repo.listByVaccination(vaccinationId);
    }

    @GetMapping("/by-date")
    @Operation(summary = "获取指定日期的提醒", description = "获取指定日期的提醒")
    public List<Reminder> byDate(@RequestParam String date) {
        LocalDate localDate = parseDate(date);
        return repo.listByDate(localDate);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取提醒详情", description = "根据ID获取提醒详情")
    public Reminder get(@PathVariable Long id) {
        return repo.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改提醒", description = "根据ID修改提醒")
    public void update(@PathVariable Long id, @RequestBody Reminder r) {
        r.setId(id);
        repo.update(r);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除提醒", description = "根据ID删除提醒")
    public void delete(@PathVariable Long id) {
        repo.delete(id);
    }

    @Autowired
    private ReminderScheduler reminderScheduler;

    @PostMapping("/trigger")
    @Operation(summary = "手动触发提醒任务", description = "手动执行定时提醒任务，立即处理到期的提醒")
    public void triggerReminderTask() {
        reminderScheduler.processDueReminders();
    }
}