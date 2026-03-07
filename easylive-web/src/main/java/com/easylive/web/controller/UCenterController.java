package com.easylive.web.controller;


import com.easylive.component.RedisComponent;
import com.easylive.config.AdminConfig;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.VideoInfoPostDTO;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.service.VideoInfoPostService;
import com.easylive.service.impl.VideoInfoFilePostServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/ucenter")

public class UCenterController extends ABaseController{

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostServiceImpl videoInfoFilePostService;
    @Resource
    private AdminConfig adminConfig;

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
        if (needDeleteVideos == null || needDeleteVideos.isEmpty())
            return getSuccessResponseVO(null);

        if (needDeleteVideos.getFirst().getStatus().equals(VideoStatusEnum.STATUS_3.getStatus()))
        {
            videoInfoPostService.deleteVideoInfoPostByVideoId(videoId);
        }else{
            //删除videoInfoPost信息
            videoInfoPostService.deleteVideoInfoPostByVideoId(videoId);
            //删除videoInfoFile信息
            VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
            filePostQuery.setVideoId(videoId);
            filePostQuery.setUserId(userId);
            List<VideoInfoFilePost> videoList = videoInfoFilePostService.findListByParam(filePostQuery);
            List<String> fileIds = videoList.stream().map(VideoInfoFilePost::getFileId).toList();
            //TODO 批量删除videoInfoFile信息
        //    videoInfoFilePostService.deleteBatchByIds(fileIds);
            //删除redis信息
            videoList.forEach(videoFile->{
                String completePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + videoFile.getFilePath();
                //占用io可以放到消息队列
                new File(completePath).delete();
                redisComponent.delUploadVideoInfo(userId, videoFile.getUploadId());
            });
        }
        return getSuccessResponseVO(null);
    }

}
