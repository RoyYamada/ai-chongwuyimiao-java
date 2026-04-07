package com.example.demo.common;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MinioUtil {

    @Autowired
    private AmazonS3 minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 生成预签名 URL
     * @param objectKey 对象键（文件名）
     * @param expirationDays 过期天数
     * @return 预签名 URL
     */
    public String generatePresignedUrl(String objectKey, int expirationDays) {
        // 计算过期时间
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * 60 * 24 * expirationDays;
        expiration.setTime(expTimeMillis);

        // 生成预签名 URL
        return minioClient.generatePresignedUrl(bucketName, objectKey, expiration).toString();
    }

    /**
     * 生成默认 7 天过期的预签名 URL
     * @param objectKey 对象键（文件名）
     * @return 预签名 URL
     */
    public String generatePresignedUrl(String objectKey) {
        return generatePresignedUrl(objectKey, 7); // 默认 7 天，AWS S3 API 限制最长为 7 天
    }
}
