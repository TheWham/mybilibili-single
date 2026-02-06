package com.easylive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AdminConfig {

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:}")
    private String account;
    @Value("${admin.password:}")
    private String password;

    @Value("${showFFmpegLog:true}")
    private Boolean showFFmpegLog;

    public Boolean getShowFFmpegLog() {
        return showFFmpegLog;
    }

    public String getAccount() {
        return account;
    }


    public String getPassword() {
        return password;
    }

    public String getProjectFolder() {
        return projectFolder;
    }
}
