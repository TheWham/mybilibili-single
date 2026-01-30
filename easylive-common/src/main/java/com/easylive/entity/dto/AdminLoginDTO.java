package com.easylive.entity.dto;


import jakarta.validation.constraints.NotEmpty;

public class AdminLoginDTO {

    @NotEmpty
    private String account;

    @NotEmpty
    private String password;

    @NotEmpty
    private String checkCode;

    @NotEmpty
    private String checkCodeKey;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getCheckCodeKey() {
        return checkCodeKey;
    }

    public void setCheckCodeKey(String checkCodeKey) {
        this.checkCodeKey = checkCodeKey;
    }
}
