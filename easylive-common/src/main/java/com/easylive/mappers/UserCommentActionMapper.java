package com.easylive.mappers;

import com.easylive.entity.vo.UserActionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author amani
 * @since 2026/03/16
 * 用户评论行为
 */
public interface UserCommentActionMapper<T, R> extends BaseMapper {

	/**
	 * 根据 ActionId查询
	 */
	T selectByActionId(@Param("actionId") Integer actionId);

	/**
	 * 根据 ActionId更新
	 */
	Integer updateByActionId(@Param("bean") T t, @Param("actionId") Integer actionId);

	/**
	 * 根据 ActionId删除
	 */
	Integer deleteByActionId(@Param("actionId") Integer actionId);


	/**
	 * 根据 CommentIdAndUserId查询
	 */
	T selectByCommentIdAndUserId(@Param("commentId") Integer commentId, @Param("userId") String userId);

	/**
	 * 根据 CommentIdAndUserId更新
	 */
	Integer updateByCommentIdAndUserId(@Param("bean") T t, @Param("commentId") Integer commentId, @Param("userId") String userId);

	/**
	 * 根据 CommentIdAndUserId删除
	 */
	Integer deleteByCommentIdAndUserId(@Param("commentId") Integer commentId, @Param("userId") String userId);

	Integer insertIgnore(@Param("bean") T userCommentAction);

	Integer selectActionTypeForUpdate(@Param("commentId") Integer commentId,@Param("userId") String userId);

	List<UserActionVO> selectActionTypeList(@Param("query") R actionQuery);
}