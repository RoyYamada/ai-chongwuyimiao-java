package com.example.demo.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenVerifier {
    @Value("${auth.token.secret:change-me}")
    private String tokenSecret;

    public String verifyAndGetOpenid(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return null;
        }
        String raw;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(bearerToken);
            raw = new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
        String[] parts = raw.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String openid = parts[0];
        long exp;
        try {
            exp = Long.parseLong(parts[1]);
        } catch (Exception e) {
            return null;
        }
        if (Instant.now().getEpochSecond() > exp) {
            return null;
        }
        String sign = parts[2];
        String data = openid + "." + exp;
        String expected = hmacSha256Base64(tokenSecret, data);
        if (!constantTimeEquals(sign, expected)) {
            return null;
        }
        return openid;
    }

    private String hmacSha256Base64(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
