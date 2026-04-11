package com.easylive.service;

import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.vo.PaginationResultVO;

public interface VideoEsService {

    void saveDoc(VideoInfo videoInfo);
    void updateDoc(VideoInfo videoInfo);
    void deleteDoc(String indexName, String videoId);
    void updateCount(String indexName, String videoId, Integer changeCount, String field);
    PaginationResultVO<VideoInfo> search(Boolean highlight, String keyword, Integer orderType, Integer pageNo, Integer pageSize);
}
