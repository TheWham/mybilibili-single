package com.easylive.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.easylive.web"}, exclude = {DataSourceAutoConfiguration.class})
public class EasyliveWebApplication{
    public static void main(String[] args) {
        SpringApplication.run(EasyliveWebApplication.class, args);
    }
}
