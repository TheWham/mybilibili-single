package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/03/21
 * 用户数量统计表
 */
public interface UserStatsMapper<T, R> extends BaseMapper {

	/**
	 * 根据 UserId查询
	 */
	T selectByUserId(@Param("userId") String userId);

	/**
	 * 根据 UserId更新
	 */
	Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

	/**
	 * 根据 UserId删除
	 */
	Integer deleteByUserId(@Param("userId") String userId);

	Integer insertOrUpdateCount(@Param("userId") String userId,@Param("field") String field,@Param("count") int count);
}