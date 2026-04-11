package com.easylive.service.impl;

import com.easylive.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.po.UserCommentAction;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.UserActionVO;
import com.easylive.entity.vo.VideoCommentVO;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.VideoCommentTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserCommentActionMapper;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.VideoCommentMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.VideoCommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author amani
 * @date 2026/03/09
 * @description 评论Service
 */

@Service("VideoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {
	@Resource
	private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoComment> findListByParam(VideoCommentQuery param) {
		List<VideoComment> list = null;

		if (param.getQueryChildren())
			list = this.selectListWithChildren(param);
		else
			list = this.videoCommentMapper.selectList(param);
		return list;
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoCommentQuery param) {
		return this.videoCommentMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoComment> list = null;

		if (param.getQueryChildren())
			list = this.selectListWithChildren(param);
		else
			list = this.videoCommentMapper.selectList(param);

		PaginationResultVO<VideoComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoComment bean) {
		return this.videoCommentMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoComment>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoCommentMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoCommentMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 回复人 评论信息, pCommentId 是要回复别人发布评论的commentID
	 * @param videoComment
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void postComment(VideoComment videoComment) {

		//表示回复评论 此时pCommentId表示要回复评论的id
		UserInfo replyUserInfo = userInfoMapper.selectByUserId(videoComment.getUserId());

		//表示回复视频评论而不是回复评论
		Integer replyCommentId = videoComment.getReplyCommentId();
		videoComment.setAvatar(replyUserInfo.getAvatar());
		if (replyCommentId == null) {
			videoComment.setPCommentId(Constants.ZERO);
		} else {
			VideoComment originalComment = videoCommentMapper.selectByCommentId(replyCommentId);

			if (originalComment == null || !originalComment.getVideoId().equals(videoComment.getVideoId())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}

			if (originalComment.getPCommentId() == Constants.ZERO.intValue())
			{
				videoComment.setPCommentId(replyCommentId);
			}else {
				videoComment.setPCommentId(originalComment.getPCommentId());
				//获取被回复人评论信息
				videoComment.setReplyUserId(originalComment.getUserId());
			}
			videoComment.setReplyNickName(videoComment.getNickName());
		}
		videoComment.setNickName(replyUserInfo.getNickName());
		//获取回复评论人信息
		videoCommentMapper.insert(videoComment);

		//只统计一级评论的数量
		if (videoComment.getPCommentId() == Constants.ZERO.intValue())
		{
			videoInfoMapper.updateCount(videoComment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), 1);
		}

	}

	@Override
	public List<VideoComment> selectListWithChildren(VideoCommentQuery param) {
		return this.videoCommentMapper.selectListWithChildren(param);
	}

	@Override
	public VideoCommentVO loadComment(String videoId, Integer pageNo, Integer orderType, TokenUserInfoDTO tokenUserInfo) {
		VideoInfo videoInfo = this.videoInfoMapper.selectByVideoId(videoId);

		if (videoInfo == null)
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())){
			return new VideoCommentVO();
		}

		VideoCommentVO videoCommentVO = new VideoCommentVO();

		VideoCommentQuery commentQuery = new VideoCommentQuery();
		commentQuery.setPCommentId(Constants.ZERO);
		commentQuery.setVideoId(videoId);
		commentQuery.setPageNo(pageNo);
		commentQuery.setQueryChildren(true);
		String orderBy = orderType == null || orderType == 0 ? "like_count desc, comment_id desc" : "comment_id desc";
		commentQuery.setOrderBy(orderBy);
		PaginationResultVO<VideoComment> commentData = this.findListByPage(commentQuery);
		List<VideoComment> allCommentData = commentData.getList();

		List<VideoComment> topComment = getTopComment(videoId);
		if (allCommentData != null && !allCommentData.isEmpty() && topComment != null && !topComment.isEmpty())
		{
			List<VideoComment> finalList = allCommentData.stream()
					.filter(item -> !item.getCommentId().equals(topComment.get(0).getCommentId()))
					.collect(Collectors.toList());
			finalList.addAll(0, topComment);
			commentData.setList(finalList);
		}
		videoCommentVO.setCommentData(commentData);
		videoCommentVO.setUserActionList(Collections.emptyList());

		if (tokenUserInfo != null)
		{
			UserActionQuery actionQuery = new UserActionQuery();
			actionQuery.setUserActionTypeList(new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType(),UserActionTypeEnum.COMMENT_HATE.getType()});
			actionQuery.setUserId(tokenUserInfo.getUserId());
			actionQuery.setVideoId(videoId);
			List<UserActionVO> actionVOList = userCommentActionMapper.selectActionTypeList(actionQuery);
			videoCommentVO.setUserActionList(actionVOList);
		}

		return videoCommentVO;
	}

	@Override
	public List<VideoComment> loadCommentUCenter(VideoCommentQuery videoCommentQuery) {
		return videoCommentMapper.selectList(videoCommentQuery);
	}

	@Override
	public Integer deleteByCommentId(Integer commentId, Boolean isAdmin, String userId) {
		VideoComment videoComment = Optional.ofNullable(this.videoCommentMapper.selectByCommentId(commentId))
				.orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

		boolean canDirectDelete = Boolean.TRUE.equals(isAdmin) || videoComment.getUserId().equals(userId) || videoComment.getVideoUserId().equals(userId);

		if (!canDirectDelete)
			throw new BusinessException(ResponseCodeEnum.CODE_600);
        return this.videoCommentMapper.deleteByCommentId(commentId);
	}

	private List<VideoComment> getTopComment(String videoId)
	{
		VideoCommentQuery commentQuery = new VideoCommentQuery();
		commentQuery.setTopType(VideoCommentTypeEnum.TOP.getType());
		commentQuery.setVideoId(videoId);
		commentQuery.setQueryChildren(true);
		List<VideoComment> topComment = findListByParam(commentQuery);
		return topComment;
	}



}
