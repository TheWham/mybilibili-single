package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表
 */
public interface UserMessageMapper<T, R> extends BaseMapper {

	/**
	 * 根据 MessageId查询
	 */
	T selectByMessageId(@Param("messageId") Integer messageId);

	/**
	 * 根据 MessageId更新
	 */
	Integer updateByMessageId(@Param("bean") T t, @Param("messageId") Integer messageId);

	/**
	 * 根据 MessageId删除
	 */
	Integer deleteByMessageId(@Param("messageId") Integer messageId);

}