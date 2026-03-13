package com.alian.emci.config;

import com.alian.emci.entity.User;
import com.alian.emci.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * H2内存数据库数据初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("h2") // 只在h2 profile下执行
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing H2 database data...");

        // 先物理删除所有用户（使用自定义SQL）
        userMapper.deleteAll();
        log.info("Cleared existing users");

        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswd(passwordEncoder.encode("abc123"));
        admin.setEmail("admin@emci.com");
        admin.setPhone("13800138000");
        admin.setType(1); // 管理员
        userMapper.insert(admin);
        log.info("Created admin user");

        // 创建普通用户
        User operator = new User();
        operator.setUsername("operator");
        operator.setPasswd(passwordEncoder.encode("abc123"));
        operator.setEmail("operator@emci.com");
        operator.setType(0); // 普通用户
        userMapper.insert(operator);
        log.info("Created operator user");

        // 创建维修人员
        User repairman = new User();
        repairman.setUsername("repairman");
        repairman.setPasswd(passwordEncoder.encode("abc123"));
        repairman.setEmail("repairman@emci.com");
        repairman.setType(0); // 普通用户
        userMapper.insert(repairman);
        log.info("Created repairman user");

        log.info("H2 database data initialization completed!");
    }
}
