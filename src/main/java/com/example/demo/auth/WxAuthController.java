package com.example.demo.auth;

import com.example.demo.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/wx")
public class WxAuthController {
    private final WxAuthService service;

    public WxAuthController(WxAuthService service) {
        this.service = service;
    }

    public static class CodeBody {
        public String code;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody CodeBody body) {
        Map<String, Object> result = service.loginByCode(body.code);
        return ApiResponse.ok(result);
    }

    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> check(@RequestParam String openid) {
        Map<String, Object> result = service.checkSessionKey(openid);
        return ApiResponse.ok(result);
    }
}
