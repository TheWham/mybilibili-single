package com.easylive.service;

import com.easylive.entity.po.VideoComment;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.vo.PaginationResultVO;

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
	 * @description 根据 CommentId查询
	 */
	VideoComment getVideoCommentByCommentId(Integer commentId);

	/**
	 * @description 根据 CommentId更新
	 */
	Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId);

	/**
	 * @description 根据 CommentId删除
	 */
	Integer deleteVideoCommentByCommentId(Integer commentId);

}