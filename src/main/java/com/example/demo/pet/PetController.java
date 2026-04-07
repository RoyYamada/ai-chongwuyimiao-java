package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.demo.common.MinioUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "宠物", description = "宠物管理相关接口")
public class PetController {
    private final PetRepository repo;
    
    @Autowired
    private MinioUtil minioUtil;

    public PetController(PetRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @Operation(summary = "创建宠物", description = "创建新的宠物信息")
    public Long create(@RequestBody Pet p) {
        return repo.create(p);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情", description = "根据ID获取宠物详情")
    public Optional<Pet> get(@PathVariable Long id) {
        Optional<Pet> petOptional = repo.findById(id);
        petOptional.ifPresent(pet -> {
            if (pet.getPhotoUrl() != null) {
                // 从 photoUrl 中提取文件名
                String photoUrl = pet.getPhotoUrl();
                String filename = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
                // 生成预签名 URL
                String presignedUrl = minioUtil.generatePresignedUrl(filename);
                pet.setPhotoUrl(presignedUrl);
            }
        });
        return petOptional;
    }

    @GetMapping
    @Operation(summary = "获取宠物列表", description = "根据主人ID获取宠物列表")
    public List<Pet> list(@RequestParam Long ownerId) {
        List<Pet> pets = repo.listByOwner(ownerId);
        // 为每个宠物生成预签名 URL
        pets.forEach(pet -> {
            if (pet.getPhotoUrl() != null) {
                // 从 photoUrl 中提取文件名
                String photoUrl = pet.getPhotoUrl();
                String filename = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
                // 生成预签名 URL
                String presignedUrl = minioUtil.generatePresignedUrl(filename);
                pet.setPhotoUrl(presignedUrl);
            }
        });
        return pets;
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改宠物信息", description = "根据ID修改宠物信息")
    public void update(@PathVariable Long id, @RequestBody Pet p) {
        p.setId(id);
        repo.update(p);
    }
}
