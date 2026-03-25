package com.example.demo.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WxAuthService {
    @Value("${wx.appid}")
    private String appId;
    @Value("${wx.secret}")
    private String appSecret;
    @Value("${auth.token.secret:change-me}")
    private String tokenSecret;
    @Value("${auth.token.ttlSeconds:2592000}")
    private long ttlSeconds;

    private final RestTemplate restTemplate = new RestTemplate();
    private final WxSessionRepository sessionRepository;

    public WxAuthService(WxSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Map<String, Object> loginByCode(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        ResponseEntity<Map> resp = restTemplate.getForEntity(URI.create(url), Map.class);
        Map body = resp.getBody();
        if (body == null) throw new RuntimeException("empty response");
        Object errcode = body.get("errcode");
        if (errcode instanceof Number && ((Number) errcode).intValue() != 0) {
            throw new RuntimeException("wx error " + errcode + " " + body.get("errmsg"));
        }
        String openid = (String) body.get("openid");
        String sessionKey = (String) body.get("session_key");
        String unionid = (String) body.get("unionid");
        sessionRepository.upsert(openid, sessionKey, unionid, Instant.now().plusSeconds(ttlSeconds));
        String token = signToken(openid, Instant.now().plusSeconds(ttlSeconds).getEpochSecond());
        Map<String, Object> out = new HashMap<>();
        out.put("token", token);
        out.put("openid", openid);
        out.put("unionid", unionid);
        return out;
    }

    public Map<String, Object> checkSessionKey(String openid) {
        String sk = sessionRepository.getSessionKey(openid);
        if (sk == null) throw new RuntimeException("session not found");
        String accessToken = getAccessToken();
        String signature = hmacSha256Base64(sk, "");
        String url = "https://api.weixin.qq.com/wxa/checksession?access_token=" + accessToken;
        Map<String, Object> req = new HashMap<>();
        req.put("openid", openid);
        req.put("signature", signature);
        req.put("sig_method", "hmac_sha256");
        ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
        Map body = resp.getBody();
        if (body == null) throw new RuntimeException("empty response");
        return body;
    }

    private String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
        ResponseEntity<Map> resp = restTemplate.getForEntity(URI.create(url), Map.class);
        Map body = resp.getBody();
        if (body == null) throw new RuntimeException("empty response");
        Object errcode = body.get("errcode");
        if (errcode instanceof Number && ((Number) errcode).intValue() != 0) {
            throw new RuntimeException("wx error " + errcode + " " + body.get("errmsg"));
        }
        return (String) body.get("access_token");
    }

    private String signToken(String openid, long exp) {
        String data = openid + "." + exp;
        String sign = hmacSha256Base64(tokenSecret, data);
        String raw = data + "." + sign;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
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
}
