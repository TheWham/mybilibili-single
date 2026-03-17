package com.easylive.service.impl;

import com.easylive.entity.po.UserFocus;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.mappers.UserFocusMapper;
import com.easylive.service.UserFocusService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表Service
 */

@Service("UserFocusService")
public class UserFocusServiceImpl implements UserFocusService {
	@Resource
	private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserFocus> findListByParam(UserFocusQuery param) {
		return this.userFocusMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserFocusQuery param) {
		return this.userFocusMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserFocus> list = this.findListByParam(param);
		PaginationResultVO<UserFocus> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserFocus bean) {
		return this.userFocusMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserFocus>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userFocusMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserFocus> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userFocusMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 UserIdAndUserFocusId查询
	 */
	@Override
	public UserFocus getUserFocusByUserIdAndUserFocusId(String userId, String userFocusId) {
		return this.userFocusMapper.selectByUserIdAndUserFocusId(userId, userFocusId);
	}

	/**
	 * 根据 UserIdAndUserFocusId更新
	 */
	@Override
	public Integer updateUserFocusByUserIdAndUserFocusId(UserFocus bean, String userId, String userFocusId) {
		return this.userFocusMapper.updateByUserIdAndUserFocusId(bean, userId, userFocusId);
	}

	/**
	 * 根据 UserIdAndUserFocusId删除
	 */
	@Override
	public Integer deleteUserFocusByUserIdAndUserFocusId(String userId, String userFocusId) {
		return this.userFocusMapper.deleteByUserIdAndUserFocusId(userId, userFocusId);
	}

}