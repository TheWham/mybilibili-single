package com.easylive.web.controller;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.annotaion.GlobalInterceptor;
import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.SeriesVideoVO;
import com.easylive.entity.vo.SeriesWithVideoUHomeVO;
import com.easylive.entity.vo.SeriesWithVideoVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserVideoSeriesService;
import com.easylive.service.UserVideoSeriesVideoService;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/uhome/series")
@GlobalInterceptor(checkLogin = true)
public class UHomeVideoSeriesController extends ABaseController{

    @Resource
    private UserVideoSeriesService userVideoSeriesService;

    @Resource
    private UserVideoSeriesVideoService userVideoSeriesVideoService;

    @RequestMapping("/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId)
    {
        List<SeriesWithVideoUHomeVO> seriesWithVideoVOS = userVideoSeriesService.selectVideoSeriesWithVideo(userId);
        return getSuccessResponseVO(seriesWithVideoVOS);
    }

    @RequestMapping("/loadVideoSeries")
    public ResponseVO loadVideoSeries(@NotEmpty String userId)
    {
        List<UserVideoSeries> videoSeriesList = userVideoSeriesService.loadVideoSeries(userId);
        return getSuccessResponseVO(videoSeriesList);
    }

    @RequestMapping("/saveVideoSeries")
    public ResponseVO saveVideoSeries(Integer seriesId,
                                          @NotEmpty @Size(max = 100) String seriesName,
                                          @Size(max = 200) String seriesDescription,
                                          String videoIds)
    {
        userVideoSeriesService.saveVideoSeries(seriesId, seriesName, seriesDescription, videoIds, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadAllVideo")
    public ResponseVO loadAllVideo(Integer seriesId)
    {
         List<VideoInfo> videoList =  userVideoSeriesService.selectAllVideoBySeriesIdAndUserId(seriesId, getTokenUserInfo().getUserId());
        List<SeriesVideoVO> seriesVideoVOS = BeanUtil.copyToList(videoList, SeriesVideoVO.class);
        return getSuccessResponseVO(seriesVideoVOS);
    }

    @RequestMapping("/changeVideoSeriesSort")
    public ResponseVO changeVideoSeriesSort(String seriesIds)
    {
        if (StringTools.isEmpty(seriesIds))
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        userVideoSeriesService.changeVideoSeriesSort(seriesIds, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/getVideoSeriesDetail")
    public ResponseVO getVideoSeriesDetail(@NotNull Integer seriesId)
    {
        String userId = getTokenUserInfo().getUserId();
        UserVideoSeriesVideoQuery videoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        videoSeriesVideoQuery.setSeriesId(seriesId);
        videoSeriesVideoQuery.setUserId(userId);
        videoSeriesVideoQuery.setQueryVideoInfo(true);
        videoSeriesVideoQuery.setOrderBy("v.sort asc");
        List<UserVideoSeriesVideo> seriesVideos = userVideoSeriesVideoService.findListByParam(videoSeriesVideoQuery);
        SeriesWithVideoVO seriesWithVideoVO = new SeriesWithVideoVO();
        UserVideoSeries videoSeries = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        seriesWithVideoVO.setSeriesVideoList(seriesVideos);
        seriesWithVideoVO.setVideoSeries(videoSeries);
        return getSuccessResponseVO(seriesWithVideoVO);
    }
    @RequestMapping("/saveSeriesVideo")
    public ResponseVO saveSeriesVideo(@NotNull Integer seriesId,
                                      Integer sort,
                                      @NotEmpty String videoIds)
    {
         userVideoSeriesService.saveSeriesVideo(seriesId, sort, videoIds, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delSeriesVideo")
    public ResponseVO delSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoId)
    {
        String userId = getTokenUserInfo().getUserId();
        UserVideoSeriesVideoQuery seriesVideoQuery = new UserVideoSeriesVideoQuery();
        seriesVideoQuery.setVideoId(videoId);
        seriesVideoQuery.setSeriesId(seriesId);
        seriesVideoQuery.setUserId(userId);
        Integer exist = userVideoSeriesVideoService.findCountByParam(seriesVideoQuery);
        if (exist == 0)
            throw new BusinessException("该视频不存在");
        Integer count = userVideoSeriesVideoService.deleteUserVideoSeriesVideoBySeriesIdAndVideoId(seriesId, videoId);
        if (count == 0)
            throw new BusinessException("删除失败");
        return getSuccessResponseVO(null);
    }

    @RequestMapping("delVideoSeries")
    public ResponseVO delVideoSeries(@NotNull Integer seriesId)
    {
        String userId = getTokenUserInfo().getUserId();
        UserVideoSeriesQuery seriesQuery = new UserVideoSeriesQuery();
        seriesQuery.setUserId(userId);
        seriesQuery.setSeriesId(seriesId);
        Integer exist = userVideoSeriesService.findCountByParam(seriesQuery);
        if (exist == 0)
            throw new BusinessException("该列表不存在");

        userVideoSeriesService.delVideoSeries(seriesId, userId);
        return getSuccessResponseVO(null);
    }
}
