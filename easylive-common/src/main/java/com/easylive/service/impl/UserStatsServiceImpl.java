package com.easylive.service.impl;

import com.easylive.entity.po.UserStats;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserStatsQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.mappers.UserStatsMapper;
import com.easylive.service.UserStatsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/21
 * 用户数量统计表Service
 */

@Service("UserStatsService")
public class UserStatsServiceImpl implements UserStatsService {
	@Resource
	private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserStats> findListByParam(UserStatsQuery param) {
		return this.userStatsMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserStatsQuery param) {
		return this.userStatsMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserStats> findListByPage(UserStatsQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserStats> list = this.findListByParam(param);
		PaginationResultVO<UserStats> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserStats bean) {
		return this.userStatsMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserStats>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userStatsMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserStats> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userStatsMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 UserId查询
	 */
	@Override
	public UserStats getUserStatsByUserId(String userId) {
		return this.userStatsMapper.selectByUserId(userId);
	}

	/**
	 * 根据 UserId更新
	 */
	@Override
	public Integer updateUserStatsByUserId(UserStats bean, String userId) {
		return this.userStatsMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据 UserId删除
	 */
	@Override
	public Integer deleteUserStatsByUserId(String userId) {
		return this.userStatsMapper.deleteByUserId(userId);
	}

}