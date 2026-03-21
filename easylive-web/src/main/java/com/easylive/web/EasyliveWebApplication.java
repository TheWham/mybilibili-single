package com.easylive.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.easylive"})
@MapperScan("com.easylive.mappers")
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class EasyliveWebApplication{
    public static void main(String[] args) {
        SpringApplication.run(EasyliveWebApplication.class, args);
    }
}
