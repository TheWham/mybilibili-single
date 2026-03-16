package com.easylive.service.impl;

import com.easylive.entity.po.UserCommentAction;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.mappers.UserCommentActionMapper;
import com.easylive.service.UserCommentActionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/16
 * 用户评论行为Service
 */

@Service("UserCommentActionService")
public class UserCommentActionServiceImpl implements UserCommentActionService {
	@Resource
	private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserCommentAction> findListByParam(UserActionQuery param) {
		return this.userCommentActionMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserActionQuery param) {
		return this.userCommentActionMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserCommentAction> findListByPage(UserActionQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserCommentAction> list = this.findListByParam(param);
		PaginationResultVO<UserCommentAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserCommentAction bean) {
		return this.userCommentActionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserCommentAction>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userCommentActionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserCommentAction> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userCommentActionMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 ActionId查询
	 */
	@Override
	public UserCommentAction getUserCommentActionByActionId(Integer actionId) {
		return this.userCommentActionMapper.selectByActionId(actionId);
	}

	/**
	 * 根据 ActionId更新
	 */
	@Override
	public Integer updateUserCommentActionByActionId(UserCommentAction bean, Integer actionId) {
		return this.userCommentActionMapper.updateByActionId(bean, actionId);
	}

	/**
	 * 根据 ActionId删除
	 */
	@Override
	public Integer deleteUserCommentActionByActionId(Integer actionId) {
		return this.userCommentActionMapper.deleteByActionId(actionId);
	}


	/**
	 * 根据 CommentIdAndUserId查询
	 */
	@Override
	public UserCommentAction getUserCommentActionByCommentIdAndUserId(Integer commentId, String userId) {
		return this.userCommentActionMapper.selectByCommentIdAndUserId(commentId, userId);
	}

	/**
	 * 根据 CommentIdAndUserId更新
	 */
	@Override
	public Integer updateUserCommentActionByCommentIdAndUserId(UserCommentAction bean, Integer commentId, String userId) {
		return this.userCommentActionMapper.updateByCommentIdAndUserId(bean, commentId, userId);
	}

	/**
	 * 根据 CommentIdAndUserId删除
	 */
	@Override
	public Integer deleteUserCommentActionByCommentIdAndUserId(Integer commentId, String userId) {
		return this.userCommentActionMapper.deleteByCommentIdAndUserId(commentId, userId);
	}

}