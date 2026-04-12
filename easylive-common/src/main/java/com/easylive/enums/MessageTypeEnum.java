package com.easylive.enums;

/**
 * 用户消息类型。
 */
public enum MessageTypeEnum {

    SYSTEM(0, "系统通知"),
    LIKE(1, "收到的赞"),
    COLLECT(2, "收到收藏"),
    COMMENT(3, "评论和@");

    private final Integer type;
    private final String desc;

    MessageTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
