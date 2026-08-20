package com.vanish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * niuniu-account 服务启动类
 */
@SpringBootApplication
@MapperScan("com.vanish.dao.mapper")
public class NiuniuAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(NiuniuAccountApplication.class, args);
    }
}
