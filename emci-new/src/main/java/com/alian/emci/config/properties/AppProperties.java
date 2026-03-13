package com.alian.emci.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    /**
     * 文件存储配置
     */
    private Storage storage = new Storage();

    @Data
    public static class Jwt {
        /**
         * JWT密钥
         */
        private String secret = "your-256-bit-secret-your-256-bit-secret";

        /**
         * Token有效期（小时）
         */
        private long expiration = 24;

        /**
         * Token前缀
         */
        private String tokenPrefix = "Bearer ";

        /**
         * 请求头名称
         */
        private String headerName = "Authorization";
    }

    @Data
    public static class Storage {
        /**
         * 存储路径
         */
        private String path = "./uploads";

        /**
         * URL前缀
         */
        private String urlPrefix = "/files";
    }
}
