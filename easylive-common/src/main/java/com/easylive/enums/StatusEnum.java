package com.easylive.enums;

import com.easylive.constants.Constants;

/**
 * @version 2026.1.8
 * @author amani
 */
public enum StatusEnum {
    DISABLE(Constants.ZERO, "禁用"),
    NORMAL(Constants.ONE, "正常");

    private Integer type;
    private String status;

    StatusEnum(int number, String status) {
        this.type = number;
        this.status = status;
    }

    public Integer getType() {
        return type;
    }


    public String getStatus() {
        return status;
    }

}
