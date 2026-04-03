package com.example.demo.wechat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@RestController
@RequestMapping("/wechat")
@Tag(name = "微信", description = "微信相关接口")
public class WeChatController {
    
    @Value("${wechat.token}")
    private String token;
    
    @GetMapping("/callback")
    @Operation(summary = "微信消息推送验证", description = "用于微信小程序消息推送配置验证")
    public String callback(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {
        
        System.out.println("=== 微信消息推送验证 ===");
        System.out.println("signature: " + signature);
        System.out.println("timestamp: " + timestamp);
        System.out.println("nonce: " + nonce);
        System.out.println("echostr: " + echostr);
        
        // 验证签名
        if (checkSignature(signature, timestamp, nonce)) {
            System.out.println("签名验证通过");
            return echostr;
        } else {
            System.out.println("签名验证失败");
            return "error";
        }
    }
    
    /**
     * 验证签名
     */
    private boolean checkSignature(String signature, String timestamp, String nonce) {
        // 1. 将token、timestamp、nonce三个参数进行字典序排序
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);
        
        System.out.println("排序后的参数: " + Arrays.toString(arr));
        
        // 2. 将三个参数字符串拼接成一个字符串
        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }
        
        System.out.println("拼接后的字符串: " + content.toString());
        
        // 3. 进行sha1计算签名
        String calculatedSignature = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            calculatedSignature = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        System.out.println("计算的签名: " + calculatedSignature);
        System.out.println("接收到的签名: " + signature);
        
        // 4. 对比签名
        return calculatedSignature != null && calculatedSignature.equals(signature);
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     */
    private String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }
    
    /**
     * 将字节转换为十六进制字符串
     */
    private String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }
}