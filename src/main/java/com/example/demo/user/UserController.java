package com.example.demo.user;

import com.example.demo.common.ApiResponse;
import com.example.demo.pet.Owner;
import com.example.demo.pet.OwnerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户", description = "用户管理相关接口")
public class UserController {
    private final OwnerRepository ownerRepository;

    @Value("${auth.token.secret:change-me}")
    private String tokenSecret;

    public UserController(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }
    
    // 验证 token 并返回 openid
    private String validateToken(HttpServletRequest request) throws Exception {
        // 获取 Authorization 头
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized: Missing or invalid token");
        }

        // 提取 token
        String token = authorization.substring(7);
        
        // 解码 token
        byte[] decoded = Base64.getUrlDecoder().decode(token);
        String raw = new String(decoded, StandardCharsets.UTF_8);
        String[] parts = raw.split("\\.");
        if (parts.length != 3) {
            throw new RuntimeException("Unauthorized: Invalid token format");
        }

        String openid = parts[0];
        long exp = Long.parseLong(parts[1]);
        String sign = parts[2];

        // 检查 token 是否过期
        if (Instant.now().getEpochSecond() > exp) {
            throw new RuntimeException("Unauthorized: Token expired");
        }

        // 验证签名
        String data = openid + "." + exp;
        String expectedSign = hmacSha256Base64(tokenSecret, data);
        if (!sign.equals(expectedSign)) {
            throw new RuntimeException("Unauthorized: Invalid token signature");
        }

        return openid;
    }
    
    private String hmacSha256Base64(String key, String data) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(out);
    }

    @PutMapping("/name")
    @Operation(summary = "更新用户名", description = "更新用户的用户名")
    public ApiResponse<Owner> updateName(HttpServletRequest request, @RequestParam String name) throws Exception {
        // 验证 token
        String openid = validateToken(request);
        
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
    public ApiResponse<Owner> updateAvatar(HttpServletRequest request, @RequestParam String avatar) throws Exception {
        // 验证 token
        String openid = validateToken(request);
        
        Optional<Owner> ownerOptional = ownerRepository.findByOpenid(openid);
        if (ownerOptional.isPresent()) {
            Owner owner = ownerOptional.get();
            // 从 avatar URL 中提取文件名，去掉 URL 参数和路径
            if (avatar != null) {
                // 提取文件名（去掉查询参数）
                int queryIndex = avatar.indexOf('?');
                if (queryIndex > 0) {
                    avatar = avatar.substring(0, queryIndex);
                }
                // 提取文件名（去掉路径）
                String filename = avatar.substring(avatar.lastIndexOf('/') + 1);
                owner.setAvatar(filename);
            }
            ownerRepository.update(owner);
            return ApiResponse.ok(owner);
        } else {
            return ApiResponse.error(404, "用户不存在");
        }
    }
}
