package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vaccinations")
@Tag(name = "疫苗接种", description = "疫苗接种管理相关接口")
public class VaccinationController {
    private final VaccinationRepository repo;
    private final VaccinationService service;
    private final VaccineRepository vaccineRepository;
    private final PetRepository petRepository;

    public VaccinationController(VaccinationRepository repo, VaccinationService service, VaccineRepository vaccineRepository, PetRepository petRepository) {
        this.repo = repo;
        this.service = service;
        this.vaccineRepository = vaccineRepository;
        this.petRepository = petRepository;
    }

    @PostMapping
    @Operation(summary = "记录疫苗接种", description = "记录新的疫苗接种信息")
    public Vaccination record(@RequestBody Vaccination v, @RequestParam Long vaccineId) {
        Vaccine meta = vaccineRepository.list(null).stream().filter(it -> it.getId().equals(vaccineId)).findFirst().orElseThrow(() -> new RuntimeException("疫苗不存在: " + vaccineId));
        Long id = service.record(v, meta);
        return repo.findById(id);
    }

    @GetMapping("/by-pet/{petId}")
    @Operation(summary = "获取宠物疫苗接种记录", description = "根据宠物ID获取疫苗接种记录")
    public List<Vaccination> byPet(@PathVariable Long petId) {
        return repo.listByPet(petId);
    }

    @GetMapping("/due")
    @Operation(summary = "获取到期疫苗接种记录", description = "获取指定日期前到期的疫苗接种记录")
    public List<Vaccination> due(@RequestParam String toDate) {
        Instant to = LocalDate.parse(toDate).atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
        return repo.listDueUntil(to);
    }

    @GetMapping("/reminders")
    @Operation(summary = "获取疫苗接种提醒", description = "获取格式化的疫苗接种提醒信息，包括未来和超时的")
    public List<VaccinationReminder> getReminders() {
        // 获取所有需要提醒的疫苗接种记录（包括未来和超时的）
        List<Vaccination> vaccinations = repo.listFutureDue();

        List<VaccinationReminder> reminders = new ArrayList<>();

        // 遍历所有接种记录，生成提醒信息
        for (Vaccination vaccination : vaccinations) {
            // 只处理状态为PENDING的记录
            if (!"PENDING".equals(vaccination.getStatus())) continue;
            
            // 获取宠物信息
            Optional<Pet> petOptional = petRepository.findById(vaccination.getPetId());
            if (!petOptional.isPresent()) continue;
            Pet pet = petOptional.get();

            // 获取疫苗信息
            List<Vaccine> vaccines = vaccineRepository.list(null);
            Vaccine vaccine = vaccines.stream()
                    .filter(v -> v.getId().equals(vaccination.getVaccineId()))
                    .findFirst()
                    .orElse(null);
            if (vaccine == null) continue;

            // 确定是基础免疫还是加强针
            String doseInfo;
            Integer doseNumber = vaccination.getDoseNumber() == null ? 0 : vaccination.getDoseNumber();
            if (doseNumber < vaccine.getDosesRequired()) {
                doseInfo = "第 " + (doseNumber + 1) + " 针";
            } else {
                doseInfo = "加强针";
            }

            // 创建提醒信息
            VaccinationReminder reminder = new VaccinationReminder(
                    vaccination.getId(),
                    pet.getName(),
                    pet.getBreed(),
                    vaccine.getName(),
                    doseInfo,
                    vaccination.getNextDueAt()
            );

            reminders.add(reminder);
        }

        return reminders;
    }

    @PostMapping("/update-status/{id}")
    @Operation(summary = "修改当前针接种状态", description = "把这一针从「待接种」改为「已接种」")
    public VaccinationService.VaccinationStatusUpdateResult updateVaccinationStatus(@PathVariable Long id) {
        return service.updateVaccinationStatus(id);
    }

    @PostMapping("/create-next-dose/auto/{id}")
    @Operation(summary = "自动创建下一针", description = "根据疫苗类型自动计算下一针的接种日期")
    public Vaccination createNextDoseAutomatically(@PathVariable Long id) {
        Long newId = service.createNextDoseAutomatically(id);
        return repo.findById(newId);
    }

    @PostMapping("/create-next-dose/manual/{id}")
    @Operation(summary = "手动创建下一针", description = "用户自己填写日期和其他信息，覆盖自动计算")
    public Vaccination createNextDoseManually(
            @PathVariable Long id,
            @RequestBody Vaccination v) {
        Long newId = service.createNextDoseManually(id, v.getNextDueAt(), v.getClinic(), v.getVetName());
        return repo.findById(newId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改接种疫苗信息", description = "根据ID修改接种疫苗信息")
    public Vaccination update(@PathVariable Long id, @RequestBody Vaccination v) {
        v.setId(id);
        repo.update(v);
        return repo.findById(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取接种记录详情", description = "根据ID获取接种记录的详细信息")
    public Vaccination getById(@PathVariable Long id) {
        return repo.findById(id);
    }
}
