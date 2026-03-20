package com.easylive.web.controller;

/**
 * @author amani
 * @since 2026.3.18
 */
public enum VideoOrderTypeEnum {

    //按照视频最新发布排序
    ORDER_POST_TIME(0, "最新发布"),
    //按照视频最多播放排序
    ORDER_PLAY_COUNT(1, "最多播放"),
    //按照视频最多收藏排序
    ORDER_COLLECT_COUNT(2,"最多收藏");

    VideoOrderTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    private Integer status;
    private String desc;


    public static VideoOrderTypeEnum getEnum(Integer type)
    {
        for (VideoOrderTypeEnum typeEnum : VideoOrderTypeEnum.values())
        {
            if (typeEnum.status.equals(type))
                return typeEnum;
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
