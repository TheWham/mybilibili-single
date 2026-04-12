package com.easylive.service;

import com.easylive.entity.po.UserMessage;
import com.easylive.entity.query.UserMessageQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表Service
 */
public interface UserMessageService {

	/**
	 * 根据条件查询
	 */
	List<UserMessage> findListByParam(UserMessageQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserMessageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param);

	/**
	 * 新增
	 */
	Integer add(UserMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserMessage>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserMessage> listBean);


	/**
	 * 根据 MessageId查询
	 */
	UserMessage getUserMessageByMessageId(Integer messageId);

	/**
	 * 根据 MessageId更新
	 */
	Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId);

	/**
	 * 根据 MessageId删除
	 */
	Integer deleteUserMessageByMessageId(Integer messageId);

}