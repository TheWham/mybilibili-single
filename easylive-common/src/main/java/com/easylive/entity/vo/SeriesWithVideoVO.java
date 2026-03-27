package com.easylive.entity.vo;

import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.UserVideoSeriesVideo;

import java.util.List;

public class SeriesWithVideoVO {
    private UserVideoSeries videoSeries;
    private List<UserVideoSeriesVideo> seriesVideoList;

    public UserVideoSeries getVideoSeries() {
        return videoSeries;
    }

    public void setVideoSeries(UserVideoSeries videoSeries) {
        this.videoSeries = videoSeries;
    }

    public List<UserVideoSeriesVideo> getSeriesVideoList() {
        return seriesVideoList;
    }

    public void setSeriesVideoList(List<UserVideoSeriesVideo> seriesVideoList) {
        this.seriesVideoList = seriesVideoList;
    }
}
