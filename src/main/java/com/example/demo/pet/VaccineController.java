package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
@Tag(name = "疫苗", description = "疫苗管理相关接口")
public class VaccineController {
    private final VaccineRepository repo;

    public VaccineController(VaccineRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @Operation(summary = "创建疫苗", description = "创建新的疫苗信息")
    public Long create(@RequestBody Vaccine v) {
        return repo.create(v);
    }

    @GetMapping
    @Operation(summary = "获取疫苗列表", description = "获取所有疫苗列表")
    public List<Vaccine> list() {
        return repo.list(null);
    }
}
