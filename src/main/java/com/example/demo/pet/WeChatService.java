package com.example.demo.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeChatService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${wechat.appid}")
    private String appid;
    
    @Value("${wechat.secret}")
    private String secret;
    
    // 存储access_token和过期时间
    private static final Map<String, AccessTokenInfo> accessTokenCache = new ConcurrentHashMap<>();
    
    public WeChatService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 获取access_token
     */
    public String getAccessToken() throws Exception {
        AccessTokenInfo info = accessTokenCache.get("access_token");
        long now = System.currentTimeMillis();
        
        // 如果缓存中有access_token且未过期，直接返回
        if (info != null && now < info.getExpireTime()) {
            System.out.println("使用缓存的access_token");
            return info.getAccessToken();
        }
        
        // 调用微信API获取access_token
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
        System.out.println("=== 获取access_token ===");
        System.out.println("请求URL: " + url);
        
        String response = restTemplate.getForObject(url, String.class);
        System.out.println("响应结果: " + response);
        
        // 解析响应
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        System.out.println("解析后的结果: " + result);
        
        if (result.containsKey("errcode")) {
            System.out.println("获取access_token失败");
            System.out.println("错误码: " + result.get("errcode"));
            System.out.println("错误信息: " + result.get("errmsg"));
            System.out.println("=== 获取access_token完成 ===");
            throw new Exception("获取access_token失败: " + result.get("errmsg"));
        }
        
        String accessToken = (String) result.get("access_token");
        int expiresIn = ((Number) result.get("expires_in")).intValue();
        System.out.println("获取access_token成功");
        System.out.println("access_token: " + accessToken);
        System.out.println("过期时间: " + expiresIn + "秒");
        
        // 缓存access_token，设置过期时间（提前100秒过期）
        accessTokenCache.put("access_token", new AccessTokenInfo(accessToken, now + (expiresIn - 100) * 1000));
        System.out.println("已缓存access_token");
        System.out.println("=== 获取access_token完成 ===");
        
        return accessToken;
    }
    
    /**
     * 发送订阅消息
     */
    public Map<String, Object> sendSubscribeMessage(String templateId, String openid, Map<String, Map<String, String>> data) throws Exception {
        String accessToken = getAccessToken();
        String urlString = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
        
        // 构建请求体
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("touser", openid);
        requestBodyMap.put("template_id", templateId);
        requestBodyMap.put("page", "pages/index/index");  // 添加page字段，跳转到小程序首页
        requestBodyMap.put("data", data);
        requestBodyMap.put("miniprogram_state", "developer");  // 改为developer开发版，测试时使用
        requestBodyMap.put("lang", "zh_CN");
        
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
        
        System.out.println("=== 发送微信订阅消息 ===");
        System.out.println("请求URL: " + urlString);
        System.out.println("请求体: " + requestBody);
        
        // 使用HttpURLConnection发送请求
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        
        // 发送请求体
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 获取响应
        int responseCode = conn.getResponseCode();
        System.out.println("HTTP响应码: " + responseCode);
        
        String response;
        if (responseCode >= 200 && responseCode < 300) {
            // 成功响应
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
                response = responseBuilder.toString();
            }
        } else {
            // 错误响应
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
                response = responseBuilder.toString();
            }
        }
        
        System.out.println("响应结果: " + response);
        
        // 解析响应
        Map<String, Object> result;
        if (response != null && !response.isEmpty()) {
            result = objectMapper.readValue(response, Map.class);
            System.out.println("解析后的结果: " + result);
            System.out.println("错误码: " + result.get("errcode"));
            System.out.println("错误信息: " + result.get("errmsg"));
        } else {
            result = new HashMap<>();
            result.put("errcode", -1);
            result.put("errmsg", "响应为空");
            System.out.println("响应为空");
        }
        System.out.println("=== 发送微信订阅消息完成 ===");
        
        return result;
    }
    
    /**
     * AccessToken信息类
     */
    private static class AccessTokenInfo {
        private final String accessToken;
        private final long expireTime;
        
        public AccessTokenInfo(String accessToken, long expireTime) {
            this.accessToken = accessToken;
            this.expireTime = expireTime;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
    }
}