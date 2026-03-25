package com.example.demo.pet;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/vaccinations")
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
    public Long record(@RequestBody Vaccination v, @RequestParam Long vaccineId) {
        Vaccine meta = vaccineRepository.list(null).stream().filter(it -> it.getId().equals(vaccineId)).findFirst().orElse(null);
        return service.record(v, meta);
    }

    @GetMapping("/by-pet/{petId}")
    public List<Vaccination> byPet(@PathVariable Long petId) {
        return repo.listByPet(petId);
    }

    @GetMapping("/due")
    public List<Vaccination> due(@RequestParam String toDate) {
        Instant to = LocalDate.parse(toDate).atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
        return repo.listDueUntil(to);
    }
}
