package com.alian.emci.service;

import com.alian.emci.vo.file.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param directory 存储目录
     * @return 文件访问URL
     */
    FileUploadVO uploadFile(MultipartFile file, String directory);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);

    /**
     * 获取文件流
     *
     * @param fileUrl 文件URL
     * @return 文件流
     */
    InputStream getFileStream(String fileUrl);

    /**
     * 上传字节数组
     *
     * @param bytes 文件字节
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param directory 存储目录
     * @return 文件访问URL
     */
    FileUploadVO uploadBytes(byte[] bytes, String fileName, String contentType, String directory);

    /**
     * 上传文件并指定文件名（覆盖已存在的文件）
     *
     * @param file 文件
     * @param fileName 指定文件名（不含扩展名）
     * @param directory 存储目录
     * @return 文件访问URL
     */
    FileUploadVO uploadFileWithName(MultipartFile file, String fileName, String directory);

    /**
     * 上传字节数组并指定文件名（覆盖已存在的文件）
     *
     * @param bytes 文件字节
     * @param fileName 指定文件名（不含扩展名）
     * @param extension 文件扩展名
     * @param contentType 内容类型
     * @param directory 存储目录
     * @return 文件访问URL
     */
    FileUploadVO uploadBytesWithName(byte[] bytes, String fileName, String extension, String contentType, String directory);
}
