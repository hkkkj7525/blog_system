// OssService.java
package com.example.blog_system.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class OssService {

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.domain}")
    private String domain;

    @Value("${aliyun.oss.max-size}")
    private long maxSize;

    public String uploadFile(MultipartFile file, String folder) {
        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = folder + "/" + UUID.randomUUID().toString() + fileExtension;

            // 上传文件
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
            ossClient.putObject(putObjectRequest);

            // 返回文件访问URL
            return domain + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatars");
    }
}