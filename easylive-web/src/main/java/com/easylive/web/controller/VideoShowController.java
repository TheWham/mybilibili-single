package com.easylive.web.controller;


import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.VideoInfoFileQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.UserActionVO;
import com.easylive.entity.vo.VideoInfoResultVO;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserVideoActionService;
import com.easylive.service.VideoEsService;
import com.easylive.service.VideoInfoFileService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
public class VideoShowController extends ABaseController{

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private UserVideoActionService userVideoActionService;
    @Resource
    private VideoEsService videoEsService;
    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/loadVideo")
    public ResponseVO loadVideo(Integer pageNo, Integer pCategoryId, Integer categoryId)
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setPCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setRecommendType(VideoRecommendEnum.NO_RECOMMEND.getStatus());
        PaginationResultVO<VideoInfo> listByPage = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo()
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendEnum.RECOMMEND.getStatus());
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }

    @RequestMapping("/getVideoInfo")
    public ResponseVO getVideoInfo(@NotEmpty String videoId){
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        VideoInfoResultVO videoInfoResultVO = new VideoInfoResultVO(videoInfo);
        //查询是否投币,点赞,收藏
        String userId = getTokenUserInfo().getUserId();
        UserActionQuery actionQuery = new UserActionQuery();
        actionQuery.setUserId(userId);
        actionQuery.setVideoId(videoId);
        actionQuery.setUserActionTypeList(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COIN.getType(), UserActionTypeEnum.VIDEO_COLLECT.getType()});
        List<UserActionVO> userActionTypeList = userVideoActionService.getUserActionTypeList(actionQuery);
        videoInfoResultVO.setUserActionList(userActionTypeList);
        return getSuccessResponseVO(videoInfoResultVO);
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId)
    {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        VideoInfoFileQuery fileQuery = new VideoInfoFileQuery();
        fileQuery.setVideoId(videoId);
        fileQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> pList = videoInfoFileService.findListByParam(fileQuery);
        return getSuccessResponseVO(pList);
    }

    @RequestMapping("/getSearchKeywordTop")
    public ResponseVO getSearchKeywordTop()
    {
        List<String> keywordTopList = redisComponent.getSearchKeywordTop(Constants.LENGTH_10);
        return getSuccessResponseVO(keywordTopList);
    }

    @RequestMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId, @NotEmpty String deviceId)
    {
        Integer count = videoInfoService.reportVideoPlayOnline(fileId, deviceId);
        return getSuccessResponseVO(count);
    }

    @RequestMapping("/search")
    public ResponseVO search(Integer pageNo, @NotEmpty String keyword, Integer orderType)
    {
        redisComponent.saveKeyword(keyword);
        PaginationResultVO<VideoInfo> search = videoEsService.search(true, keyword, orderType, pageNo, PageSize.SIZE30.getSize());
        return getSuccessResponseVO(search);
    }

    @RequestMapping("/getVideoRecommend")
    public ResponseVO getVideoRecommend(@NotEmpty String keyword, @NotEmpty String videoId)
    {
        List<VideoInfo> search = videoEsService.search(false, keyword, SearchOrderTypeEnum.VIDEO_PLAY.getStatus(), 1, PageSize.SIZE30.getSize()).getList();
        List<VideoInfo> list = search.stream().filter(video -> !video.getVideoId().equals(videoId)).collect(Collectors.toList());
        return getSuccessResponseVO(list);
    }

    @RequestMapping("/loadHotVideoList")
    public ResponseVO loadHotVideoList(Integer pageNo)
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("play_count desc");
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        PaginationResultVO<VideoInfo> list = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(list);
    }

}

