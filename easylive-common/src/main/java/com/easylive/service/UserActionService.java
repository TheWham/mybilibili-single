package com.easylive.service;

import com.easylive.entity.po.UserAction;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论Service
 */
public interface UserActionService {

	/**
	 * @description 根据条件查询
	 */
	List<UserAction> findListByParam(UserActionQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(UserActionQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<UserAction> findListByPage(UserActionQuery param);

	/**
	 * @description 新增
	 */
	Integer add(UserAction bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<UserAction>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserAction> listBean);


	/**
	 * @description 根据 ActionId查询
	 */
	UserAction getUserActionByActionId(Integer actionId);

	/**
	 * @description 根据 ActionId更新
	 */
	Integer updateUserActionByActionId(UserAction bean, Integer actionId);

	/**
	 * @description 根据 ActionId删除
	 */
	Integer deleteUserActionByActionId(Integer actionId);


	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId查询
	 */
	UserAction getUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId);

	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId更新
	 */
	Integer updateUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(UserAction bean, String videoId, Integer commentId, Integer actionType, String userId);

	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId删除
	 */
	Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId);

    void doAction(UserAction userAction);
}