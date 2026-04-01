package com.easylive.service;

import com.easylive.entity.po.VideoInfo;

public interface VideoEsService {

    void saveDoc(VideoInfo videoInfo);
    void updateDoc(VideoInfo videoInfo);
}
