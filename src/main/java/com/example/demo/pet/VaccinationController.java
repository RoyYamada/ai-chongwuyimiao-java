package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vaccinations")
@Tag(name = "疫苗接种", description = "疫苗接种管理相关接口")
public class VaccinationController {
    private final VaccinationRepository repo;
    private final VaccinationService service;
    private final VaccineRepository vaccineRepository;
    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public VaccinationController(VaccinationRepository repo, VaccinationService service, VaccineRepository vaccineRepository, PetRepository petRepository, OwnerRepository ownerRepository) {
        this.repo = repo;
        this.service = service;
        this.vaccineRepository = vaccineRepository;
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    private Owner currentOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        return ownerRepository.findByOpenid(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
    }

    private Pet requireOwnedPet(Long petId) {
        Owner owner = currentOwner();
        Pet pet = petRepository.findById(petId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pet not found"));
        if (pet.getOwnerId() != null && !pet.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        return pet;
    }

    private Vaccination requireOwnedVaccination(Long vaccinationId) {
        Vaccination v;
        try {
            v = repo.findById(vaccinationId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        }
        requireOwnedPet(v.getPetId());
        return v;
    }

    @PostMapping
    @Operation(summary = "记录疫苗接种", description = "记录新的疫苗接种信息")
    public Vaccination record(@RequestBody Vaccination v, @RequestParam Long vaccineId) {
        requireOwnedPet(v.getPetId());
        Vaccine meta = vaccineRepository.list(null).stream().filter(it -> it.getId().equals(vaccineId)).findFirst().orElseThrow(() -> new RuntimeException("疫苗不存在: " + vaccineId));
        Long id = service.record(v, meta);
        return repo.findById(id);
    }

    @GetMapping("/by-pet/{petId}")
    @Operation(summary = "获取宠物疫苗接种记录", description = "根据宠物ID获取疫苗接种记录")
    public List<Vaccination> byPet(@PathVariable Long petId) {
        requireOwnedPet(petId);
        return repo.listByPet(petId);
    }

    @GetMapping("/due")
    @Operation(summary = "获取到期疫苗接种记录", description = "获取状态是已接种的疫苗接种记录，支持分页")
    public Map<String, Object> due(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Owner owner = currentOwner();
        List<Vaccination> vaccinations = repo.listDueCompletedByOwner(owner.getId(), page, size);
        int total = repo.countCompletedVaccinationsByOwner(owner.getId());
        
        // 转换为与reminders接口相同的格式
        List<VaccinationReminder> reminders = new ArrayList<>();
        for (Vaccination vaccination : vaccinations) {
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
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", reminders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @GetMapping("/reminders")
    @Operation(summary = "获取疫苗接种提醒", description = "获取格式化的疫苗接种提醒信息，包括未来和超时的")
    public List<VaccinationReminder> getReminders() {
        // 获取所有需要提醒的疫苗接种记录（包括未来和超时的）
        Owner owner = currentOwner();
        List<Vaccination> vaccinations = repo.listFutureDueByOwner(owner.getId());

        Map<Long, Pet> petById = petRepository.listByOwner(owner.getId())
                .stream()
                .collect(Collectors.toMap(Pet::getId, Function.identity(), (a, b) -> a));
        Map<Long, Vaccine> vaccineById = vaccineRepository.list(null)
                .stream()
                .collect(Collectors.toMap(Vaccine::getId, Function.identity(), (a, b) -> a));

        List<VaccinationReminder> reminders = new ArrayList<>();
        for (Vaccination vaccination : vaccinations) {
            Pet pet = petById.get(vaccination.getPetId());
            if (pet == null) continue;
            Vaccine vaccine = vaccineById.get(vaccination.getVaccineId());
            if (vaccine == null) continue;

            String doseInfo;
            Integer doseNumber = vaccination.getDoseNumber() == null ? 0 : vaccination.getDoseNumber();
            Integer required = vaccine.getDosesRequired() == null ? 1 : vaccine.getDosesRequired();
            if (doseNumber < required) {
                doseInfo = "第 " + (doseNumber + 1) + " 针";
            } else {
                doseInfo = "加强针";
            }

            reminders.add(new VaccinationReminder(
                    vaccination.getId(),
                    pet.getName(),
                    pet.getBreed(),
                    vaccine.getName(),
                    doseInfo,
                    vaccination.getNextDueAt()
            ));
        }

        return reminders;
    }

    @PostMapping("/update-status/{id}")
    @Operation(summary = "修改当前针接种状态", description = "把这一针从「待接种」改为「已接种」")
    public VaccinationService.VaccinationStatusUpdateResult updateVaccinationStatus(@PathVariable Long id) {
        requireOwnedVaccination(id);
        return service.updateVaccinationStatus(id);
    }

    @PostMapping("/create-next-dose/auto/{id}")
    @Operation(summary = "自动创建下一针", description = "根据疫苗类型自动计算下一针的接种日期")
    public Vaccination createNextDoseAutomatically(@PathVariable Long id) {
        requireOwnedVaccination(id);
        Long newId = service.createNextDoseAutomatically(id);
        return repo.findById(newId);
    }

    @PostMapping("/create-next-dose/manual/{id}")
    @Operation(summary = "手动创建下一针", description = "用户自己填写日期和其他信息，覆盖自动计算")
    public Vaccination createNextDoseManually(
            @PathVariable Long id,
            @RequestBody Vaccination v) {
        requireOwnedVaccination(id);
        Long newId = service.createNextDoseManually(id, v.getNextDueAt(), v.getClinic(), v.getVetName());
        return repo.findById(newId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改接种疫苗信息", description = "根据ID修改接种疫苗信息")
    public Vaccination update(@PathVariable Long id, @RequestBody Vaccination v) {
        requireOwnedVaccination(id);
        v.setId(id);
        repo.update(v);
        return repo.findById(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取接种记录详情", description = "根据ID获取接种记录的详细信息")
    public Vaccination getById(@PathVariable Long id) {
        requireOwnedVaccination(id);
        return repo.findById(id);
    }

    @DeleteMapping("/reminders/{id}")
    @Operation(summary = "删除疫苗接种提醒", description = "删除指定疫苗接种记录的提醒，将 next_due_at 置空并删除相关提醒记录")
    public void deleteReminder(@PathVariable Long id) {
        requireOwnedVaccination(id);
        service.deleteReminder(id);
    }
}
