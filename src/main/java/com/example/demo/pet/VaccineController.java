package com.example.demo.pet;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
public class VaccineController {
    private final VaccineRepository repo;

    public VaccineController(VaccineRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Long create(@RequestBody Vaccine v) {
        return repo.create(v);
    }

    @GetMapping
    public List<Vaccine> list(@RequestParam(required = false) String species) {
        return repo.list(species);
    }
}
