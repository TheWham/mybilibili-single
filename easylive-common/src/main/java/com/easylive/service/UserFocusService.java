package com.easylive.service;

import com.easylive.entity.po.UserFocus;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表Service
 */
public interface UserFocusService {

	/**
	 * 根据条件查询
	 */
	List<UserFocus> findListByParam(UserFocusQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserFocusQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param);

	/**
	 * 新增
	 */
	Integer add(UserFocus bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserFocus>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserFocus> listBean);


	/**
	 * 根据 UserIdAndUserFocusId查询
	 */
	UserFocus getUserFocusByUserIdAndUserFocusId(String userId, String userFocusId);

	/**
	 * 根据 UserIdAndUserFocusId更新
	 */
	Integer updateUserFocusByUserIdAndUserFocusId(UserFocus bean, String userId, String userFocusId);

	/**
	 * 根据 UserIdAndUserFocusId删除
	 */
	Integer deleteUserFocusByUserIdAndUserFocusId(String userId, String userFocusId);

}