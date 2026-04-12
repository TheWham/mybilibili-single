package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史
 */
public interface VideoPlayHistoryMapper<T, R> extends BaseMapper {

	/**
	 * 根据 UserIdAndVideoId查询
	 */
	T selectByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

	/**
	 * 根据 UserIdAndVideoId更新
	 */
	Integer updateByUserIdAndVideoId(@Param("bean") T t, @Param("userId") String userId, @Param("videoId") String videoId);

	/**
	 * 根据 UserIdAndVideoId删除
	 */
	Integer deleteByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

}