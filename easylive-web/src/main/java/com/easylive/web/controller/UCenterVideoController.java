package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.VideoAuditCountVO;
import com.easylive.entity.vo.VideoInfoPostEditVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.service.VideoInfoPostService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ucenter")
public class UCenterVideoController extends ABaseController{

    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private VideoInfoService videoInfoService;

    //getVideoCountInfo
    @RequestMapping("loadVideoList")
    public ResponseVO loadVideoList(Integer pageNo, String videoNameFuzzy, Integer status)
    {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfo.getUserId());
        videoInfoPostQuery.setPageNo(pageNo);
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setOrderBy("v.create_time desc");
        if (status != null && status == -1)
        {
            videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS_3.getStatus(), VideoStatusEnum.STATUS_4.getStatus()});
        }else {
            videoInfoPostQuery.setStatus(status);
        }
        videoInfoPostQuery.setQueryCountInfo(true);
        return getSuccessResponseVO(videoInfoPostService.findListByPage(videoInfoPostQuery));
    }


    /**
     * 获取视频所处各自状态的数量
     * @return
     */

    @RequestMapping("getVideoCountInfo")
    public ResponseVO getVideoCountInfo()
    {
        VideoAuditCountVO videoAuditCountVO = videoInfoPostService.getVideoCountInfo(getTokenUserInfo().getUserId());
        return getSuccessResponseVO(videoAuditCountVO);
    }

    @RequestMapping("getVideoByVideoId")
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId)
    {
        String userId = getTokenUserInfo().getUserId();
        VideoInfoPost videoInfoPost = this.videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !videoInfoPost.getUserId().equals(userId))
            throw new BusinessException(ResponseCodeEnum.CODE_404);

        VideoInfoFilePostQuery fileQuery = new VideoInfoFilePostQuery();
        fileQuery.setVideoId(videoId);
        fileQuery.setOrderBy("file_index asc");
        fileQuery.setUserId(userId);
        List<VideoInfoFilePost> videoInfoFileList = videoInfoFilePostService.findListByParam(fileQuery);


        VideoInfoPostEditVO videoInfoPostEditVO = new VideoInfoPostEditVO();

        videoInfoPostEditVO.setVideoInfo(videoInfoPost);
        videoInfoPostEditVO.setVideoInfoFileList(videoInfoFileList);
        return getSuccessResponseVO(videoInfoPostEditVO);
    }
    
}


