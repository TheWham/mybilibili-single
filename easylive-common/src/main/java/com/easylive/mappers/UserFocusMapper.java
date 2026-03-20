package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表
 */
public interface UserFocusMapper<T, R> extends BaseMapper {

	/**
	 * 根据 UserIdAndUserFocusId查询
	 */
	T selectByUserIdAndUserFocusId(@Param("userId") String userId, @Param("userFocusId") String userFocusId);

	/**
	 * 根据 UserIdAndUserFocusId更新
	 */
	Integer updateByUserIdAndUserFocusId(@Param("bean") T t, @Param("userId") String userId, @Param("userFocusId") String userFocusId);

	/**
	 * 根据 UserIdAndUserFocusId删除
	 */
	Integer deleteByUserIdAndUserFocusId(@Param("userId") String userId, @Param("userFocusId") String userFocusId);

    Integer insertIgnore(@Param("bean") T userFocus);
}