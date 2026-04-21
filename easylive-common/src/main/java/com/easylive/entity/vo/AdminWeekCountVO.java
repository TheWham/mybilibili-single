package com.easylive.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author amani
 * @since 2026.4.22
 * 管理端首页近 7 天趋势
 */
public class AdminWeekCountVO {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date statisticsDate;

    private Integer statisticsCount;

    public Date getStatisticsDate() {
        return statisticsDate;
    }

    public void setStatisticsDate(Date statisticsDate) {
        this.statisticsDate = statisticsDate;
    }

    public Integer getStatisticsCount() {
        return statisticsCount;
    }

    public void setStatisticsCount(Integer statisticsCount) {
        this.statisticsCount = statisticsCount;
    }
}
