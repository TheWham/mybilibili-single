package com.easylive.enums;


/**
 * @version 2026.3.7
 * @author amani
 * 视频推荐状态枚举类
 * 用于表示视频的推荐状态，包括未推荐和已推荐两种状态
 */
public enum VideoRecommendEnum {
    // 未推荐状态，值为0
    NO_RECOMMEND(0, "未推荐"),
    // 已推荐状态，值为1
    RECOMMEND(1, "已推荐");

    // 推荐状态值
    private Integer status;
    // 推荐状态描述
    private String desc;

    /**
     * 构造方法
     * @param status 推荐状态值
     * @param desc 推荐状态描述
     */
    VideoRecommendEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 获取推荐状态值
     * @return 推荐状态值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 获取推荐状态描述
     * @return 推荐状态描述
     */
    public String getDesc() {
        return desc;
    }
}
