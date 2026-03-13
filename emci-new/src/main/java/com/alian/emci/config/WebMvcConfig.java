package com.alian.emci.config;

import com.alian.emci.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 * 配置静态资源映射
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用配置的路径（绝对路径）
        String uploadsPath = appProperties.getStorage().getPath();
        // 确保路径分隔符统一为 /
        String resourcePath = "file:" + uploadsPath.replace("\\", "/") + "/";

        // 映射 /uploads/** 到本地文件系统
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);

        // 注意：/api/files/** 由 FileController 处理，不需要静态资源映射
        // 前端通过 /api/files/... 访问文件时，由 FileController.getFile 处理

        System.out.println("静态资源映射: /uploads/** -> " + resourcePath);
        System.out.println("FileController 处理: /api/files/** -> " + uploadsPath);
        System.out.println("存储路径确认: " + uploadsPath);
        System.out.println("目录是否存在: " + new File(uploadsPath).exists());
    }
}
