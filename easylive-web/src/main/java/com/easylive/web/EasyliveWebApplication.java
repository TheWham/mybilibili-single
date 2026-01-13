package com.easylive.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.easylive"})
@MapperScan("com.easylive.mappers")
public class EasyliveWebApplication{
    public static void main(String[] args) {
        SpringApplication.run(EasyliveWebApplication.class, args);
    }
}
