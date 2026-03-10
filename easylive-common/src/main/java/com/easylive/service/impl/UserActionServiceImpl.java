package com.easylive.service.impl;

import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserActionMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserActionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论Service
 */

@Service("UserActionService")
public class UserActionServiceImpl implements UserActionService {
	@Resource
	private UserActionMapper<UserAction, UserActionQuery> userActionMapper;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<UserAction> findListByParam(UserActionQuery param) {
		return this.userActionMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserActionQuery param) {
		return this.userActionMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<UserAction> findListByPage(UserActionQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserAction> list = this.findListByParam(param);
		PaginationResultVO<UserAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(UserAction bean) {
		return this.userActionMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<UserAction>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userActionMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserAction> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userActionMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 ActionId查询
	 */
	@Override
	public UserAction getUserActionByActionId(Integer actionId) {
		return this.userActionMapper.selectByActionId(actionId);
	}

	/**
	 * @description 根据 ActionId更新
	 */
	@Override
	public Integer updateUserActionByActionId(UserAction bean, Integer actionId) {
		return this.userActionMapper.updateByActionId(bean, actionId);
	}

	/**
	 * @description 根据 ActionId删除
	 */
	@Override
	public Integer deleteUserActionByActionId(Integer actionId) {
		return this.userActionMapper.deleteByActionId(actionId);
	}


	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId查询
	 */
	@Override
	public UserAction getUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
	}

	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId更新
	 */
	@Override
	public Integer updateUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(UserAction bean, String videoId, Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.updateByVideoIdAndCommentIdAndActionTypeAndUserId(bean, videoId, commentId, actionType, userId);
	}

	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId删除
	 */
	@Override
	public Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.deleteByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void doAction(UserAction userAction) {
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
		UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(userAction.getActionType());

		if (videoInfo == null || actionTypeEnum == null)
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		UserActionQuery actionQuery = new UserActionQuery();
		actionQuery.setActionType(userAction.getActionType());
		actionQuery.setVideoId(userAction.getVideoId());
		actionQuery.setUserId(userAction.getUserId());
		Integer actionId = userActionMapper.selectSingleAction(actionQuery);

		if (actionId != null){
			videoInfoMapper.updateCount(userAction.getVideoId(), actionTypeEnum.getField(), -1);
			deleteUserActionByActionId(actionId);
			return;
		}

		//更新数量
		videoInfoMapper.updateCount(userAction.getVideoId(), actionTypeEnum.getField(), userAction.getActionCount());

		//将action存入db
		userAction.setVideoUserId(videoInfo.getUserId());
		add(userAction);
	}

}