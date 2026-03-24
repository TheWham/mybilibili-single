package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/03/18
 * 用户视频列表
 */
public interface UserVideoSeriesMapper<T, R> extends BaseMapper {

	/**
	 * 根据 SeriesId查询
	 */
	T selectBySeriesId(@Param("seriesId") Integer seriesId);

	/**
	 * 根据 SeriesId更新
	 */
	Integer updateBySeriesId(@Param("bean") T t, @Param("seriesId") Integer seriesId);

	/**
	 * 根据 SeriesId删除
	 */
	Integer deleteBySeriesId(@Param("seriesId") Integer seriesId);

    Integer selectMaxSort(@Param("userId") String userId);
}