package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    @Autowired
    private AmazonS3 minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostMapping("/pet-photo")
    @Operation(summary = "上传宠物照片", description = "上传宠物照片并返回照片URL")
    public ResponseEntity<String> uploadPetPhoto(@RequestParam("file") MultipartFile file) {
        logger.info("开始上传宠物照片，文件名: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            logger.warn("上传的文件为空");
            return ResponseEntity.badRequest().body("请选择要上传的文件");
        }

        try {
            // 确保存储桶存在
            logger.info("检查存储桶是否存在: {}", bucketName);
            if (!minioClient.doesBucketExistV2(bucketName)) {
                logger.info("存储桶不存在，创建存储桶: {}", bucketName);
                minioClient.createBucket(bucketName);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + extension;
            logger.info("生成文件名: {}", filename);

            // 创建临时文件
            File tempFile = File.createTempFile("upload-", filename);
            logger.info("创建临时文件: {}", tempFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }
            
            // 上传到MinIO，设置 ACL 为 PublicRead
            logger.info("上传文件到MinIO，存储桶: {}, 文件名: {}", bucketName, filename);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename, tempFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            minioClient.putObject(putObjectRequest);
            tempFile.delete();
            logger.info("临时文件已删除: {}", tempFile.getAbsolutePath());
            
            // 生成普通文件 URL
            String photoUrl = minioClient.getUrl(bucketName, filename).toString();
            logger.info("文件上传成功，URL: {}", photoUrl);

            return ResponseEntity.ok(photoUrl);
        } catch (IOException e) {
            logger.error("上传失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败：" + e.getMessage());
        }
    }
}