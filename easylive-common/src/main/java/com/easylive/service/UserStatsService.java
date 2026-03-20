package com.easylive.service;

import com.easylive.entity.po.UserStats;
import com.easylive.entity.query.UserStatsQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/21
 * 用户数量统计表Service
 */
public interface UserStatsService {

	/**
	 * 根据条件查询
	 */
	List<UserStats> findListByParam(UserStatsQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserStatsQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserStats> findListByPage(UserStatsQuery param);

	/**
	 * 新增
	 */
	Integer add(UserStats bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserStats>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserStats> listBean);


	/**
	 * 根据 UserId查询
	 */
	UserStats getUserStatsByUserId(String userId);

	/**
	 * 根据 UserId更新
	 */
	Integer updateUserStatsByUserId(UserStats bean, String userId);

	/**
	 * 根据 UserId删除
	 */
	Integer deleteUserStatsByUserId(String userId);

}