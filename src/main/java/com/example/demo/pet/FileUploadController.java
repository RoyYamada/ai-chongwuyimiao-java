package com.example.demo.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    private static final String UPLOAD_DIR = "uploads/pet-photos/";

    @PostMapping("/pet-photo")
    @Operation(summary = "上传宠物照片", description = "上传宠物照片并返回照片URL")
    public ResponseEntity<String> uploadPetPhoto(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件");
        }

        try {
            // 获取项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            // 构建上传目录的绝对路径
            String uploadDirPath = projectRoot + File.separator + UPLOAD_DIR;
            // 创建上传目录
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + extension;

            // 保存文件
            File dest = new File(uploadDir, filename);
            file.transferTo(dest);

            // 动态构建服务器地址
            String scheme = request.getScheme(); // http or https
            String serverName = request.getServerName(); // 服务器域名或IP
            int serverPort = request.getServerPort(); // 服务器端口
            String contextPath = request.getContextPath(); // 上下文路径
            
            // 构建基础URL
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(scheme).append("://").append(serverName);
            if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
                baseUrl.append(":").append(serverPort);
            }
            baseUrl.append(contextPath);
            
            // 生成访问URL
            String photoUrl = baseUrl.toString() + "/uploads/pet-photos/" + filename;

            return ResponseEntity.ok(photoUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败：" + e.getMessage());
        }
    }
}