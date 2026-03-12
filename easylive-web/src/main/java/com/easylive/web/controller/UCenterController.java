package com.easylive.web.controller;


import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.VideoInfoPostDTO;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.service.VideoInfoPostService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ucenter")

public class UCenterController extends ABaseController{

    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @RequestMapping("/postVideo")
    @Transactional
    public ResponseVO postVideo(@Validated VideoInfoPostDTO videoInfoPostDTO)
    {
        videoInfoPostDTO.setUserId(getTokenUserInfo().getUserId());
        videoInfoPostService.savePostVideoInfo(videoInfoPostDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/testdeleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId)
    {

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        String userId = tokenUserInfo.getUserId();
        VideoInfoPostQuery query = new VideoInfoPostQuery();
        query.setVideoId(videoId);
        query.setUserId(userId);
        query.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS_3.getStatus(), VideoStatusEnum.STATUS_4.getStatus()});
        List<VideoInfoPost> needDeleteVideos = videoInfoPostService.findListByParam(query);
        videoInfoFilePostService.deleVideo(needDeleteVideos, videoId, userId);
        return getSuccessResponseVO(null);
    }

}
