package com.easylive.entity.vo;

/**
 * @author amani
 * @since 2026.4.22
 * 管理端首页统计总览
 */
public class AdminIndexStatisticsVO {
    /**
     * 当前全站统计
     */
    private AdminTotalCountInfoVO totalCountInfo;

    /**
     * 昨日全站统计数据数组
     */
    private Integer[] preDayData;

    public AdminTotalCountInfoVO getTotalCountInfo() {
        return totalCountInfo;
    }

    public void setTotalCountInfo(AdminTotalCountInfoVO totalCountInfo) {
        this.totalCountInfo = totalCountInfo;
    }

    public Integer[] getPreDayData() {
        return preDayData;
    }

    public void setPreDayData(Integer[] preDayData) {
        this.preDayData = preDayData;
    }
}
