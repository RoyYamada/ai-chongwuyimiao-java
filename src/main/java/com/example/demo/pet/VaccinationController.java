package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/vaccinations")
@Tag(name = "疫苗接种", description = "疫苗接种管理相关接口")
public class VaccinationController {
    private final VaccinationRepository repo;
    private final VaccinationService service;
    private final VaccineRepository vaccineRepository;

    public VaccinationController(VaccinationRepository repo, VaccinationService service, VaccineRepository vaccineRepository) {
        this.repo = repo;
        this.service = service;
        this.vaccineRepository = vaccineRepository;
    }

    @PostMapping
    @Operation(summary = "记录疫苗接种", description = "记录新的疫苗接种信息")
    public Long record(@RequestBody Vaccination v, @RequestParam Long vaccineId) {
        Vaccine meta = vaccineRepository.list(null).stream().filter(it -> it.getId().equals(vaccineId)).findFirst().orElse(null);
        return service.record(v, meta);
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
}
