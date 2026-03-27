package com.example.demo.auth;

import com.example.demo.common.ApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/wx")
@Tag(name = "微信认证", description = "微信登录和会话管理相关接口")
public class WxAuthController {
    private final WxAuthService service;

    public WxAuthController(WxAuthService service) {
        this.service = service;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeBody {
        public String code;
    }

    @PostMapping("/login")
    @Operation(summary = "微信登录", description = "通过微信code进行登录，获取token和用户信息")
    public ApiResponse<Map<String, Object>> login(@RequestBody CodeBody body) {
        Map<String, Object> result = service.loginByCode(body.code);
        return ApiResponse.ok(result);
    }
}
