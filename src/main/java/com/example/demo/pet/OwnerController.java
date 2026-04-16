package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/owners")
@Tag(name = "宠物主人", description = "宠物主人管理相关接口")
public class OwnerController {
    private final OwnerRepository repo;

    public OwnerController(OwnerRepository repo) {
        this.repo = repo;
    }

    private Owner currentOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        return repo.findByOpenid(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息", description = "根据 token 获取当前登录用户信息")
    public Owner me() {
        return currentOwner();
    }

    @PutMapping("/me")
    @Operation(summary = "更新当前登录用户信息", description = "更新当前登录用户的基本信息")
    public void updateMe(@RequestBody Owner o) {
        Owner current = currentOwner();
        o.setId(current.getId());
        o.setOpenid(current.getOpenid());
        o.setUnionid(current.getUnionid());
        repo.update(o);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物主人详情", description = "仅允许获取当前登录用户自己的信息")
    public Optional<Owner> get(@PathVariable Long id) {
        Owner current = currentOwner();
        if (!current.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        return repo.findById(id);
    }
}
