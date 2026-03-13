package com.alian.emci.service.impl;

import com.alian.emci.config.properties.AppProperties;
import com.alian.emci.service.FileStorageService;
import com.alian.emci.vo.file.FileUploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 * 用于开发和测试环境，不依赖腾讯云COS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final AppProperties appProperties;

    private String getStoragePath() {
        return appProperties.getStorage().getPath();
    }

    private String getUrlPrefix() {
        return appProperties.getStorage().getUrlPrefix();
    }

    @Override
    public FileUploadVO uploadFile(MultipartFile file, String directory) {
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + "." + extension;

        Path filePath = writeFile(file, directory, fileName, false);
        String fileUrl = buildFileUrl(directory, fileName);

        log.info("文件上传成功 (本地): {}", fileUrl);

        return buildUploadVO(fileUrl, originalName, file.getSize(), file.getContentType());
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(getUrlPrefix(), "");
            Path filePath = Paths.get(getStoragePath(), relativePath);
            Files.deleteIfExists(filePath);
            log.info("文件删除成功 (本地): {}", filePath);
        } catch (IOException e) {
            log.error("文件删除失败", e);
        }
    }

    @Override
    public InputStream getFileStream(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(getUrlPrefix(), "");
            Path filePath = Paths.get(getStoragePath(), relativePath);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("获取文件流失败", e);
            return null;
        }
    }

    @Override
    public FileUploadVO uploadBytes(byte[] bytes, String fileName, String contentType, String directory) {
        Path filePath = writeFile(bytes, directory, fileName, false);
        String fileUrl = buildFileUrl(directory, fileName);

        log.info("字节上传成功 (本地): {}", fileUrl);

        return buildUploadVO(fileUrl, fileName, (long) bytes.length, contentType);
    }

    @Override
    public FileUploadVO uploadFileWithName(MultipartFile file, String fileName, String directory) {
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String newFileName = fileName + "." + extension;

        Path filePath = writeFile(file, directory, newFileName, true);
        String fileUrl = buildFileUrl(directory, newFileName);

        log.info("文件上传成功 (覆盖模式): {}", fileUrl);

        return buildUploadVO(fileUrl, originalName, file.getSize(), file.getContentType());
    }

    @Override
    public FileUploadVO uploadBytesWithName(byte[] bytes, String fileName, String extension, String contentType, String directory) {
        String newFileName = fileName + "." + extension;

        Path filePath = writeFile(bytes, directory, newFileName, true);
        String fileUrl = buildFileUrl(directory, newFileName);

        log.info("字节上传成功 (覆盖模式): {}", fileUrl);

        return buildUploadVO(fileUrl, newFileName, (long) bytes.length, contentType);
    }

    // ==================== 私有辅助方法 ====================

    private Path writeFile(MultipartFile file, String directory, String fileName, boolean overwrite) {
        try {
            Path dirPath = Paths.get(getStoragePath(), directory);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);
            if (overwrite) {
                Files.deleteIfExists(filePath);
            }
            Files.write(filePath, file.getBytes());
            return filePath;
        } catch (IOException e) {
            log.error("文件写入失败", e);
            throw new RuntimeException("文件写入失败: " + e.getMessage());
        }
    }

    private Path writeFile(byte[] bytes, String directory, String fileName, boolean overwrite) {
        try {
            Path dirPath = Paths.get(getStoragePath(), directory);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);
            if (overwrite) {
                Files.deleteIfExists(filePath);
            }
            Files.write(filePath, bytes);
            return filePath;
        } catch (IOException e) {
            log.error("文件写入失败", e);
            throw new RuntimeException("文件写入失败: " + e.getMessage());
        }
    }

    private String buildFileUrl(String directory, String fileName) {
        return getUrlPrefix() + "/" + directory + "/" + fileName;
    }

    private FileUploadVO buildUploadVO(String fileUrl, String originalName, long size, String contentType) {
        return FileUploadVO.builder()
                .url(fileUrl)
                .originalName(originalName)
                .size(size)
                .contentType(contentType)
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
