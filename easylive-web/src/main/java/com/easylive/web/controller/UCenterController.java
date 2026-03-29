package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.VideoInfoPostDTO;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.VideoAuditCountVO;
import com.easylive.entity.vo.VideoInfoPostEditVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private VideoCommentService videoCommentService;
    @Autowired
    @Resource
    private VideoDanmuService videoDanmuService;

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

    @RequestMapping("/postVideo")
    @Transactional
    public ResponseVO postVideo(@Validated VideoInfoPostDTO videoInfoPostDTO)
    {
        videoInfoPostDTO.setUserId(getTokenUserInfo().getUserId());
        videoInfoPostService.savePostVideoInfo(videoInfoPostDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId)
    {

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        String userId = tokenUserInfo.getUserId();
        videoInfoFilePostService.deleVideo(videoId, userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/saveVideoInteraction")
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction)
    {
        VideoInfoPost videoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        videoInfoPost.setInteraction(interaction);
        videoInfoPostService.saveVideoInteraction(videoInfoPost);
        return getSuccessResponseVO(null);

    }
    @RequestMapping("/loadAllVideo")
    public ResponseVO loadAllVideo()
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(getTokenUserInfo().getUserId());
        List<VideoInfo> list = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(list);
    }
    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoId)
    {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setQueryChildren(false);
        videoCommentQuery.setQueryUserInfo(true);
        videoCommentQuery.setPageSize(pageSize);
        videoCommentQuery.setOrderBy("v.comment_id desc");
        PaginationResultVO<VideoComment> listByPage = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoId)
    {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setPageSize(pageSize);
        videoDanmuQuery.setVideoUserId(getTokenUserInfo().getUserId());
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setQueryUserInfo(true);
        videoDanmuQuery.setOrderBy("v.time asc");
        PaginationResultVO<VideoDanmu> list = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVO(list);
    }

}


