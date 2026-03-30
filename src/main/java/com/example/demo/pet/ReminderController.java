package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@Tag(name = "提醒", description = "疫苗接种提醒相关接口")
public class ReminderController {
    private final ReminderRepository repo;
    private final VaccinationRepository vaccinationRepository;
    private final PetRepository petRepository;
    private final VaccineRepository vaccineRepository;

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
    @Operation(summary = "根据疫苗接种记录创建提醒", description = "根据疫苗接种记录创建提醒，只需要提供template_id和vaccination_id")
    public Long createFromVaccination(@RequestParam String templateId, @RequestParam Long vaccinationId) {
        // 获取疫苗接种记录
        Vaccination vaccination = vaccinationRepository.findById(vaccinationId);
        if (vaccination == null) {
            throw new RuntimeException("疫苗接种记录不存在");
        }
        
        // 获取宠物信息
        Pet pet = petRepository.findById(vaccination.getPetId())
                .orElseThrow(() -> new RuntimeException("宠物不存在"));
        
        // 获取疫苗信息
        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(vaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("疫苗不存在"));
        
        // 创建提醒
        Reminder reminder = new Reminder();
        reminder.setTemplateId(templateId);
        reminder.setVaccinationId(vaccinationId);
        reminder.setReminderDate(vaccination.getNextDueAt().atZone(ZoneId.systemDefault()).toLocalDate());
        
        // 构建提醒事项
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

    @GetMapping("/by-vaccination/{vaccinationId}")
    @Operation(summary = "获取疫苗接种的提醒", description = "根据疫苗接种ID获取相关的提醒")
    public List<Reminder> byVaccination(@PathVariable Long vaccinationId) {
        return repo.listByVaccination(vaccinationId);
    }

    @GetMapping("/by-date")
    @Operation(summary = "获取指定日期的提醒", description = "获取指定日期的提醒")
    public List<Reminder> byDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
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
}