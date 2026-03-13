package com.alian.emci;

import com.alian.emci.config.properties.AppProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * EMCI主启动类
 */
@SpringBootApplication
@MapperScan("com.alian.emci.mapper")
@EnableConfigurationProperties(AppProperties.class)
public class EmciApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmciApplication.class, args);
    }
}
