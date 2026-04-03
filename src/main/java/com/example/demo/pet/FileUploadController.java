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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    
    @Autowired
    private AmazonS3 minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostMapping("/pet-photo")
    @Operation(summary = "上传宠物照片", description = "上传宠物照片并返回照片URL")
    public ResponseEntity<String> uploadPetPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件");
        }

        try {
            // 确保存储桶存在
            if (!minioClient.doesBucketExistV2(bucketName)) {
                minioClient.createBucket(bucketName);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + extension;

            // 创建临时文件
            File tempFile = File.createTempFile("upload-", filename);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }
            
            // 上传到MinIO
            minioClient.putObject(new PutObjectRequest(bucketName, filename, tempFile));
            tempFile.delete();
            
            // 生成访问URL
            String photoUrl = minioClient.getUrl(bucketName, filename).toString();

            return ResponseEntity.ok(photoUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败：" + e.getMessage());
        }
    }
}