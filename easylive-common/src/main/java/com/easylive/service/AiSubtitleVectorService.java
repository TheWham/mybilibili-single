package com.easylive.service;

import com.easylive.entity.vo.AiMatchedVideoVO;

import java.util.List;

public interface AiSubtitleVectorService {

    List<AiMatchedVideoVO> search(List<Double> queryVector, Integer topK, Double minScore);

    void deleteByVideoId(String videoId);
}
