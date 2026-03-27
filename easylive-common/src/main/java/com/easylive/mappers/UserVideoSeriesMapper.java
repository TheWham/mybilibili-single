package com.easylive.mappers;

import com.easylive.entity.vo.SeriesWithVideoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
	 * 根据 SeriesIdAndUserId删除
	 */
	Integer deleteBySeriesIdAndUserId(@Param("seriesId") Integer seriesId,@Param("userId") String userId);

    Integer selectMaxSort(@Param("userId") String userId);

	List<T> loadVideoSeries(@Param("userId") String userId);

	Integer updateSeriesSortBatch(@Param("list") List<T> updateList,@Param("userId") String userId);

	List<SeriesWithVideoVO> selectVideoSeriesWithVideo(@Param("userId") String userId);
}