package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/owners")
@Tag(name = "宠物主人", description = "宠物主人管理相关接口")
public class OwnerController {
    private final OwnerRepository repo;

    public OwnerController(OwnerRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @Operation(summary = "创建宠物主人", description = "创建新的宠物主人信息")
    public Long create(@RequestBody Owner o) {
        return repo.create(o);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物主人详情", description = "根据ID获取宠物主人详情")
    public Optional<Owner> get(@PathVariable Long id) {
        return repo.findById(id);
    }
}
