package com.easylive.web.controller;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.VideoCommentDTO;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.VideoCommentVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoCommentService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author amani
 * @date 2026/03/09
 * @description 评论Service
 */

@RestController
@RequestMapping("comment")
public class VideoCommentController extends ABaseController {
	@Resource
	private VideoCommentService videoCommentService;
	@Resource
	private VideoInfoService videoInfoService;

	/**
	 *
	 * @param videoId 视频id
	 * @param pageNo 页码
	 * @param orderType 优先显示置顶 其次按照0表示按照点赞数量, 1:表示评论发出时间 排序
	 * @return
	 */
	@RequestMapping("loadComment")
	public ResponseVO loadComment (@NotEmpty String videoId, Integer pageNo,@NotNull Integer orderType) {
		VideoCommentVO videoCommentVO = videoCommentService.loadComment(videoId, pageNo, orderType, getTokenUserInfo());
		return getSuccessResponseVO(videoCommentVO);
	}

	/**
	 * postComment 发布评论
	 */

	@RequestMapping("postComment")
	public ResponseVO postComment(@Validated VideoCommentDTO videoCommentDTO)
	{
		//获取发布视频信息
		VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoCommentDTO.getVideoId());

		if (videoInfo == null)
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())){
			throw new BusinessException("up主已关闭评论区");
		}
		//回复人信息
		VideoComment videoComment = BeanUtil.toBean(videoCommentDTO, VideoComment.class);
		videoComment.setUserId(getTokenUserInfo().getUserId());
		videoComment.setPostTime(new Date());
		videoComment.setVideoUserId(videoInfo.getUserId());
		this.videoCommentService.postComment(videoComment);
		return getSuccessResponseVO(videoComment);
	}

	@RequestMapping("/userDelComment")
	public ResponseVO userDelComment(@NotNull Integer commentId)
	{
		 videoCommentService.deleteByCommentId(commentId, false, getTokenUserInfo().getUserId());
		 return getSuccessResponseVO(null);
	}

}