package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.common.MinioUtil;

import java.util.List;
import java.util.Optional;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "宠物", description = "宠物管理相关接口")
public class PetController {
    private final PetRepository repo;
    
    @Autowired
    private MinioUtil minioUtil;
    
    private final OwnerRepository ownerRepository;

    public PetController(PetRepository repo, OwnerRepository ownerRepository) {
        this.repo = repo;
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

    @PostMapping
    @Operation(summary = "创建宠物", description = "创建新的宠物信息")
    public Long create(@RequestBody Pet p) {
        Owner owner = currentOwner();
        p.setOwnerId(owner.getId());
        
        // 从 photoUrl 中提取文件名，去掉 URL 参数和路径
        if (p.getPhotoUrl() != null) {
            String photoUrl = p.getPhotoUrl();
            // 提取文件名（去掉查询参数）
            int queryIndex = photoUrl.indexOf('?');
            if (queryIndex > 0) {
                photoUrl = photoUrl.substring(0, queryIndex);
            }
            // 提取文件名（去掉路径）
            String filename = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
            p.setPhotoUrl(filename);
        }
        return repo.create(p);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情", description = "根据ID获取宠物详情")
    public Optional<Pet> get(@PathVariable Long id) {
        Owner owner = currentOwner();
        Optional<Pet> petOptional = repo.findById(id);
        petOptional.ifPresent(pet -> {
            if (pet.getOwnerId() != null && !pet.getOwnerId().equals(owner.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
            }
            if (pet.getPhotoUrl() != null) {
                // 直接使用存储的文件名生成预签名 URL
                String presignedUrl = minioUtil.generatePresignedUrl(pet.getPhotoUrl());
                pet.setPhotoUrl(presignedUrl);
            }
        });
        return petOptional;
    }

    @GetMapping
    @Operation(summary = "获取宠物列表", description = "获取当前用户的宠物列表")
    public List<Pet> list() {
        Owner owner = currentOwner();
        List<Pet> pets = repo.listByOwner(owner.getId());
        
        // 为每个宠物生成预签名 URL
        pets.forEach(pet -> {
            if (pet.getPhotoUrl() != null) {
                // 直接使用存储的文件名生成预签名 URL
                String presignedUrl = minioUtil.generatePresignedUrl(pet.getPhotoUrl());
                pet.setPhotoUrl(presignedUrl);
            }
        });
        return pets;
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改宠物信息", description = "根据ID修改宠物信息")
    public void update(@PathVariable Long id, @RequestBody Pet p) {
        Owner owner = currentOwner();
        Optional<Pet> existing = repo.findById(id);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        }
        if (existing.get().getOwnerId() != null && !existing.get().getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        
        // 从 photoUrl 中提取文件名，去掉 URL 参数和路径
        if (p.getPhotoUrl() != null) {
            String photoUrl = p.getPhotoUrl();
            // 提取文件名（去掉查询参数）
            int queryIndex = photoUrl.indexOf('?');
            if (queryIndex > 0) {
                photoUrl = photoUrl.substring(0, queryIndex);
            }
            // 提取文件名（去掉路径）
            String filename = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
            p.setPhotoUrl(filename);
        }
        
        // 设置宠物 ID 和主人 ID
        p.setId(id);
        p.setOwnerId(owner.getId());
        
        repo.update(p);
    }
}
