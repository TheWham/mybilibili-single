package com.easylive.entity.vo;

/**
 * @author amani
 * 返回给前端用户中心首页视频数据
 */
public class UCenterVideoDateVO {
    /**
     * 当前用户视频数据
     */
    private TotalCountInfoVO totalCountInfo;

    /**
     * 昨天用户视频数据
     */
    private TotalCountInfoVO preDayData;

    public TotalCountInfoVO getTotalCountInfo() {
        return totalCountInfo;
    }

    public void setTotalCountInfo(TotalCountInfoVO totalCountInfo) {
        this.totalCountInfo = totalCountInfo;
    }

    public TotalCountInfoVO getPreDayData() {
        return preDayData;
    }

    public void setPreDayData(TotalCountInfoVO preDayData) {
        this.preDayData = preDayData;
    }
}
