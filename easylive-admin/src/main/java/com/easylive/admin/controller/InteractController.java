package com.easylive.admin.controller;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.query.VideoDanmuQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoCommentService;
import com.easylive.service.VideoDanmuService;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @since 2026.4.22
 * @author amani
 */
@RestController
@RequestMapping("/interact")
public class InteractController extends ABaseController{

    private final VideoCommentService videoCommentService;

    private final VideoDanmuService videoDanmuService;


    public InteractController(VideoCommentService videoCommentService, VideoDanmuService videoDanmuService) {
        this.videoCommentService = videoCommentService;
        this.videoDanmuService = videoDanmuService;
    }

    @RequestMapping("loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoNameFuzzy)
    {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setPageSize(pageSize);
        // 后台评论管理走扁平列表查询，不走前台那套树形评论加载。
        // 这样会进入 selectList，并把 queryUserInfo 相关的联表字段一并带出来。
        videoCommentQuery.setQueryChildren(false);
        videoCommentQuery.setQueryUserInfo(true);
        videoCommentQuery.setOrderBy("v.post_time desc");
        PaginationResultVO<VideoComment> listByPage = videoCommentService.findListByPage(videoCommentQuery);
        List<VideoComment> list = listByPage.getList();
        if (videoNameFuzzy != null)
        {
            list =  list.stream().filter(item -> item.getVideoName().contains(videoNameFuzzy)).toList();
            listByPage.setList(list);
        }
        return getSuccessResponseVO(listByPage);
    }
    @RequestMapping("loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoNameFuzzy)
    {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setPageSize(pageSize);
        videoDanmuQuery.setOrderBy("v.post_time desc");
        videoDanmuQuery.setQueryUserInfo(true);
        PaginationResultVO<VideoDanmu> listByPage = videoDanmuService.findListByPage(videoDanmuQuery);
        List<VideoDanmu> list = listByPage.getList();
        if (videoNameFuzzy != null)
        {
            list =  list.stream().filter(item -> item.getVideoName().contains(videoNameFuzzy)).toList();
            listByPage.setList(list);
        }
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("delDanmu")
    @MessageInterceptor(sendMessage = false)
    public ResponseVO delDanmu(@NotNull Integer danmuId)
    {
        Integer ans = videoDanmuService.deleteVideoDanmuByDanmuId(danmuId, true, null);
        if (ans == 0)
            throw new BusinessException("删除弹幕失败");
        return getSuccessResponseVO(null);
    }

    @RequestMapping("delComment")
    @MessageInterceptor(sendMessage = false)
    public ResponseVO delComment(@NotNull Integer commentId)
    {
        Integer ans = videoCommentService.deleteByCommentId(commentId, true, null);
        if (ans == 0)
            throw new BusinessException("删除评论失败");
        return getSuccessResponseVO(null);
    }

}
