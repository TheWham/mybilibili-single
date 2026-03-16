package com.easylive.service;

import com.easylive.entity.po.UserCommentAction;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/16
 * 用户评论行为Service
 */
public interface UserCommentActionService {

	/**
	 * 根据条件查询
	 */
	List<UserCommentAction> findListByParam(UserActionQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserActionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserCommentAction> findListByPage(UserActionQuery param);

	/**
	 * 新增
	 */
	Integer add(UserCommentAction bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserCommentAction>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserCommentAction> listBean);


	/**
	 * 根据 ActionId查询
	 */
	UserCommentAction getUserCommentActionByActionId(Integer actionId);

	/**
	 * 根据 ActionId更新
	 */
	Integer updateUserCommentActionByActionId(UserCommentAction bean, Integer actionId);

	/**
	 * 根据 ActionId删除
	 */
	Integer deleteUserCommentActionByActionId(Integer actionId);


	/**
	 * 根据 CommentIdAndUserId查询
	 */
	UserCommentAction getUserCommentActionByCommentIdAndUserId(Integer commentId, String userId);

	/**
	 * 根据 CommentIdAndUserId更新
	 */
	Integer updateUserCommentActionByCommentIdAndUserId(UserCommentAction bean, Integer commentId, String userId);

	/**
	 * 根据 CommentIdAndUserId删除
	 */
	Integer deleteUserCommentActionByCommentIdAndUserId(Integer commentId, String userId);

}