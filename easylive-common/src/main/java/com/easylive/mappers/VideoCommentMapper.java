package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @date 2026/03/09
 * @description 评论
 */
public interface VideoCommentMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 CommentId查询
	 */
	T selectByCommentId(@Param("commentId") Integer commentId);

	/**
	 * @description 根据 CommentId更新
	 */
	Integer updateByCommentId(@Param("bean") T t, @Param("commentId") Integer commentId);

	/**
	 * @description 根据 CommentId删除
	 */
	Integer deleteByCommentId(@Param("commentId") Integer commentId);

}