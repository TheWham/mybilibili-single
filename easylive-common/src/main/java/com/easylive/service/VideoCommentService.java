package com.easylive.service;

import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.VideoCommentVO;

import java.util.List;


/**
 * @author amani
 * @date 2026/03/09
 * @description 评论Service
 */
public interface VideoCommentService {

	/**
	 * @description 根据条件查询
	 */
	List<VideoComment> findListByParam(VideoCommentQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(VideoCommentQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param);

	/**
	 * @description 新增
	 */
	Integer add(VideoComment bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<VideoComment>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoComment> listBean);

	/**
	 * 发布评论
	 */
    void postComment(VideoComment videoComment);

	/**
	 * 级联查询子集合
	 */
	List<VideoComment> selectListWithChildren(VideoCommentQuery param);

	VideoCommentVO loadComment(String videoId, Integer pageNo, Integer orderType, TokenUserInfoDTO tokenUserInfoDTO);

	List<VideoComment> loadCommentUCenter(VideoCommentQuery videoCommentQuery);

	Integer deleteByCommentId(Integer commentId, Boolean isAdmin, String userId);
}