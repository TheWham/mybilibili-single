package com.easylive.entity.vo;

import com.easylive.entity.po.VideoInfo;

public class VideoInfoResultVO {
    private VideoInfo videoInfo;

    public VideoInfoResultVO() {
    }

    public VideoInfoResultVO(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
}
