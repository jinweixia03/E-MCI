package com.alian.emci.controller;

import com.alian.emci.common.Result;
import com.alian.emci.config.properties.AppProperties;
import com.alian.emci.service.FileStorageService;
import com.alian.emci.vo.file.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 提供本地存储文件的HTTP访问和上传
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传和访问接口")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final AppProperties appProperties;
    private final FileStorageService fileStorageService;

    /**
     * 通用文件上传接口
     */
    @Operation(summary = "上传文件", description = "上传通用文件到指定目录")
    @PostMapping("/upload")
    public Result<FileUploadVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "repair") String directory) {

        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null) {
            return Result.error("无法识别文件类型");
        }

        // 只允许图片文件
        if (!contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        try {
            FileUploadVO result = fileStorageService.uploadFile(file, directory);
            log.info("文件上传成功: {}, 目录: {}", file.getOriginalFilename(), directory);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/**")
    public ResponseEntity<byte[]> getFile(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String filePath = requestUri.substring(contextPath.length() + "/files".length());

        log.info("文件访问请求 - URI: {}, contextPath: {}, filePath: {}", requestUri, contextPath, filePath);

        Path storagePath = Paths.get(appProperties.getStorage().getPath()).normalize();
        Path fullPath = storagePath.resolve(filePath.substring(1)).normalize(); // 去掉开头的/

        log.info("存储路径: {}, 完整路径: {}", storagePath, fullPath);

        // 安全检查：确保文件在存储目录内
        if (!fullPath.startsWith(storagePath)) {
            log.warn("非法路径访问: {}, 存储路径: {}, 完整路径: {}", filePath, storagePath, fullPath);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            if (!Files.exists(fullPath)) {
                log.warn("文件不存在: {}", fullPath);
                return ResponseEntity.notFound().build();
            }

            byte[] content = Files.readAllBytes(fullPath);

            // 检测Content-Type
            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("读取文件失败: {}", fullPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
