package com.example.demo.pet;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetRepository repo;

    public PetController(PetRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Long create(@RequestBody Pet p) {
        return repo.create(p);
    }

    @GetMapping("/{id}")
    public Optional<Pet> get(@PathVariable Long id) {
        return repo.findById(id);
    }

    @GetMapping
    public List<Pet> list(@RequestParam Long ownerId) {
        return repo.listByOwner(ownerId);
    }
}
