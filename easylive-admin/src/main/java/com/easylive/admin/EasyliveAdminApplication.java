package com.easylive.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.easylive")
@MapperScan("com.easylive.mappers")
@EnableTransactionManagement
@EnableScheduling
public class EasyliveAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyliveAdminApplication.class, args);
    }
}
