package com.easylive.web.controller;


import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.VideoRecommendEnum;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoShowController extends ABaseController{

    @Resource
    private VideoInfoService videoInfoService;

    @RequestMapping("/loadVideo")
    public ResponseVO loadVideo(Integer pageNo, Integer pCategoryId, Integer categoryId)
    {

        return null;
    }

    @RequestMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo()
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setRecommendType(VideoRecommendEnum.RECOMMEND.getStatus());
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }
}
