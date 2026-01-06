package com.easylive.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.easylive.admin", exclude = {DataSourceAutoConfiguration.class})
public class EasyliveAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyliveAdminApplication.class, args);
    }
}
