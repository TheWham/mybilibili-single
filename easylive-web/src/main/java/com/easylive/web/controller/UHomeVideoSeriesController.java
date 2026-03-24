package com.easylive.web.controller;

import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.UserVideoSeriesService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/uhome/series")
public class UHomeVideoSeriesController extends ABaseController{

    @Resource
    private UserVideoSeriesService userVideoSeriesService;



    //TODO loadVideoSeriesWithVideo
    @RequestMapping("/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId)
    {
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadVideoSeries")
    public ResponseVO loadVideoSeries(@NotEmpty String userId)
    {
        UserVideoSeriesQuery videoSeriesQuery= new UserVideoSeriesQuery();
        videoSeriesQuery.setUserId(userId);
        List<UserVideoSeries> videoSeriesList = userVideoSeriesService.findListByParam(videoSeriesQuery);
        return getSuccessResponseVO(videoSeriesList);
    }

    @RequestMapping("/saveUserVideoSeries")
    public ResponseVO saveUserVideoSeries(Integer seriesId,
                                          @NotEmpty @Size(max = 100) String seriesName,
                                          @Size(max = 200) String seriesDescription,
                                          String videoIds)
    {
        userVideoSeriesService.saveUserVideoSeries(seriesId, seriesName, seriesDescription, videoIds, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadAllVideo")
    public ResponseVO loadAllVideo(@NotNull Integer seriesId)
    {
         List<VideoInfo> videoList =  userVideoSeriesService.selectAllVideoBySeriesId(seriesId, getTokenUserInfo().getUserId());
         return getSuccessResponseVO(videoList);
    }
}
