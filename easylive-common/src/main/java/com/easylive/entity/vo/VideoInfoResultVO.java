package com.easylive.entity.vo;

import com.easylive.entity.po.VideoInfo;

import java.util.List;

public class VideoInfoResultVO {
    private VideoInfo videoInfo;

    private List<UserActionVO> userActionList;

    public List<UserActionVO> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserActionVO> userActionList) {
        this.userActionList = userActionList;
    }

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
