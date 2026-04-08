package com.example.demo.user;

import com.example.demo.common.ApiResponse;
import com.example.demo.pet.Owner;
import com.example.demo.pet.OwnerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户", description = "用户管理相关接口")
public class UserController {
    private final OwnerRepository ownerRepository;

    public UserController(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @PutMapping("/name")
    @Operation(summary = "更新用户名", description = "更新用户的用户名")
    public ApiResponse<Owner> updateName(@RequestParam String openid, @RequestParam String name) {
        Optional<Owner> ownerOptional = ownerRepository.findByOpenid(openid);
        if (ownerOptional.isPresent()) {
            Owner owner = ownerOptional.get();
            owner.setName(name);
            ownerRepository.update(owner);
            return ApiResponse.ok(owner);
        } else {
            return ApiResponse.error(404, "用户不存在");
        }
    }

    @PutMapping("/avatar")
    @Operation(summary = "更新用户头像", description = "更新用户的头像")
    public ApiResponse<Owner> updateAvatar(@RequestParam String openid, @RequestParam String avatar) {
        Optional<Owner> ownerOptional = ownerRepository.findByOpenid(openid);
        if (ownerOptional.isPresent()) {
            Owner owner = ownerOptional.get();
            owner.setAvatar(avatar);
            ownerRepository.update(owner);
            return ApiResponse.ok(owner);
        } else {
            return ApiResponse.error(404, "用户不存在");
        }
    }
}
