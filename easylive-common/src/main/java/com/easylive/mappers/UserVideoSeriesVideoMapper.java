package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频
 */
public interface UserVideoSeriesVideoMapper<T, R> extends BaseMapper {

	/**
	 * 根据 SeriesIdAndVideoId查询
	 */
	T selectBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

	/**
	 * 根据 SeriesIdAndVideoId更新
	 */
	Integer updateBySeriesIdAndVideoId(@Param("bean") T t, @Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

	/**
	 * 根据 SeriesIdAndVideoId删除
	 */
	Integer deleteBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

}