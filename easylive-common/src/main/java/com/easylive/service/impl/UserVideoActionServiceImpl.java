package com.easylive.service.impl;

import com.easylive.entity.po.UserVideoAction;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.UserActionVO;
import com.easylive.enums.PageSize;
import com.easylive.mappers.UserVideoActionMapper;
import com.easylive.service.UserVideoActionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论Service
 */

@Service("UserVideoActionService")
public class UserVideoActionServiceImpl implements UserVideoActionService {

	@Resource
	private UserVideoActionMapper<UserVideoAction, UserActionQuery> userVideoActionMapper;



	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<UserVideoAction> findListByParam(UserActionQuery param) {
		return this.userVideoActionMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserActionQuery param) {
		return this.userVideoActionMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<UserVideoAction> findListByPage(UserActionQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoAction> list = this.findListByParam(param);
		PaginationResultVO<UserVideoAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(UserVideoAction bean) {
		return this.userVideoActionMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<UserVideoAction>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoActionMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserVideoAction> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoActionMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 ActionId查询
	 */
	@Override
	public UserVideoAction getUserActionByActionId(Integer actionId) {
		return this.userVideoActionMapper.selectByActionId(actionId);
	}

	/**
	 * @description 根据 ActionId更新
	 */
	@Override
	public Integer updateUserActionByActionId(UserVideoAction bean, Integer actionId) {
		return this.userVideoActionMapper.updateByActionId(bean, actionId);
	}

	/**
	 * @description 根据 ActionId删除
	 */
	@Override
	public Integer deleteUserActionByActionId(Integer actionId) {
		return this.userVideoActionMapper.deleteByActionId(actionId);
	}


	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId查询
	 */
	@Override
	public UserVideoAction getUserActionByVideoIdAndActionTypeAndUserId(String videoId, Integer actionType, String userId) {
		return this.userVideoActionMapper.selectByVideoIdAndActionTypeAndUserId(videoId, actionType, userId);
	}

	/**
	 * @description 根据 VideoIdAndActionTypeAndUserId删除
	 */
	@Override
	public Integer deleteUserActionByVideoIdAndActionTypeAndUserId(String videoId,Integer actionType, String userId) {
		return this.userVideoActionMapper.deleteByVideoIdAndActionTypeAndUserId(videoId, actionType, userId);
	}


	@Override
	public List<UserActionVO> getUserActionTypeList(UserActionQuery actionQuery) {
		return userVideoActionMapper.selectActionTypeList(actionQuery);
	}

}
