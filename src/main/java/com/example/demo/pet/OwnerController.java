package com.example.demo.pet;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {
    private final OwnerRepository repo;

    public OwnerController(OwnerRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Long create(@RequestBody Owner o) {
        return repo.create(o);
    }

    @GetMapping("/{id}")
    public Optional<Owner> get(@PathVariable Long id) {
        return repo.findById(id);
    }
}
