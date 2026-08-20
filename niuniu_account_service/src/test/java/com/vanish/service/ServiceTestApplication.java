package com.vanish.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * service 模块单元测试专用启动类（扫描整个 com.vanish 包）
 */
@SpringBootApplication(scanBasePackages = "com.vanish")
@MapperScan("com.vanish.dao.mapper")
public class ServiceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceTestApplication.class, args);
    }
}
