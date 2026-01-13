package com.easylive.enums;

import com.easylive.entity.constants.Constants;

public enum SexEnum {
    MALE(Constants.ONE,"男性"),
    FEMALE(Constants.ZERO, "女性"),
    UNKNOWN(Constants.TWO, "未知");

    private Integer type;
    private String msg;
    SexEnum(Integer type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public Integer getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }
}
