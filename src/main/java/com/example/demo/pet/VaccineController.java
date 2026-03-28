package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/batch")
    @Operation(summary = "批量导入疫苗", description = "批量导入多个疫苗信息")
    public Map<String, Object> batchCreate(@RequestBody List<Vaccine> vaccines) {
        int count = 0;
        for (Vaccine vaccine : vaccines) {
            repo.create(vaccine);
            count++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "成功导入 " + count + " 个疫苗");
        return result;
    }
}
