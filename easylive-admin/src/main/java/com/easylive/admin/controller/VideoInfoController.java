package com.easylive.admin.controller;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.service.VideoInfoPostService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/videoInfo")
public class VideoInfoController extends ABaseController {

    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private VideoInfoService videoInfoService;

    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(Integer pageNo, Integer pageSize, String videoNameFuzzy, Integer recommendType, Integer categoryId) {
        VideoInfoPostQuery postQuery = new VideoInfoPostQuery();
        postQuery.setPageNo(pageNo);
        postQuery.setPageSize(pageSize);
        postQuery.setQueryCountInfo(true);
        postQuery.setQueryUserInfo(true);
        postQuery.setVideoNameFuzzy(videoNameFuzzy);
        postQuery.setCategoryId(categoryId);
        postQuery.setRecommendType(recommendType);
        postQuery.setOrderBy("v.last_update_time desc");
        PaginationResultVO<VideoInfoPost> listByPage = videoInfoPostService.findListByPage(postQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(String videoId)
    {
        VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
        filePostQuery.setVideoId(videoId);
        List<VideoInfoFilePost> pFilePost = videoInfoFilePostService.findListByParam(filePostQuery);
        return getSuccessResponseVO(pFilePost);

    }

    @RequestMapping("/auditVideo")
    @Transactional(rollbackFor = Exception.class)
    @MessageInterceptor
    public ResponseVO auditVideo(String videoId, Integer status, String reason)
    {
       videoInfoService.auditVideo(videoId, status, reason);
       return getSuccessResponseVO(null);
    }

    @RequestMapping("/recommendVideo")
    public ResponseVO recommendVideo(@NotEmpty String videoId)
    {
        videoInfoService.recommendVideo(videoId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId)
    {
        videoInfoFilePostService.deleVideo(videoId, null, true);
        return getSuccessResponseVO(null);
    }

}
