package com.easylive.enums;

public enum VideoFileUpdateTypeEnum {
    UPDATE(1, "更新"),
    UN_UPDATE(0, "未更新");

    private Integer status;
    private String desc;

    VideoFileUpdateTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
