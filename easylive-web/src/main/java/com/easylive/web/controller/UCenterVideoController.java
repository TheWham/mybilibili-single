package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.VideoAuditCountVO;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.service.VideoInfoPostService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ucenter")
public class UCenterVideoController extends ABaseController{

    @Resource
    private VideoInfoPostService videoInfoPostService;
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

    @RequestMapping("getVideoCountInfo")
    public ResponseVO getVideoCountInfo()
    {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo();
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDTO.getUserId());
        Integer status = VideoStatusEnum.STATUS_3.getStatus();
        videoInfoPostQuery.setStatus(status);
        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        status = VideoStatusEnum.STATUS_4.getStatus();
        videoInfoPostQuery.setStatus(status);
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS_3.getStatus(), VideoStatusEnum.STATUS_4.getStatus()});
        videoInfoPostQuery.setStatus(null);
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        VideoAuditCountVO videoAuditCountVO = new VideoAuditCountVO();
        videoAuditCountVO.setAuditFailCount(auditFailCount);
        videoAuditCountVO.setInProgress(inProgress);
        videoAuditCountVO.setAuditPassCount(auditPassCount);
        return getSuccessResponseVO(videoAuditCountVO);
    }
}


