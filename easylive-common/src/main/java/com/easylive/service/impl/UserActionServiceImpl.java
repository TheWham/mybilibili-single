package com.easylive.service.impl;

import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.UserActionVO;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserActionMapper;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserActionService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger log = LoggerFactory.getLogger(UserActionServiceImpl.class);
	@Resource
	private UserActionMapper<UserAction, UserActionQuery> userActionMapper;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

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
	 * @description 根据 VideoIdAndActionTypeAndUserId删除
	 */
	@Override
	public Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId,Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.deleteByVideoIdAndCommentIdAndActionTypeAndUserId(videoId,commentId, actionType, userId);
	}

	/**
	 * 用户点击点赞
	 *    ↓
	 * 请求到服务
	 *    ↓
	 * Redis记录点赞状态
	 *    ↓
	 * 立即返回成功
	 *    ↓
	 * MQ发送消息
	 *    ↓
	 * 异步写MySQL
	 * 更新之后解决高并发版本 没有加入redis mq 只是单纯用mysql抗压
	 * 从之前先select在更新变成先insert判断解决数据不一致问题
	 *
	 */

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void doAction(UserAction userAction)
	{
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
		UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(userAction.getActionType());

		if (videoInfo == null || actionTypeEnum == null)
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		userAction.setVideoUserId(videoInfo.getUserId());
		//TODO 引入redis存储点赞数量异步放到MQ处理消息

		switch (actionTypeEnum) {
			case VIDEO_LIKE:
			case VIDEO_COLLECT:
				handleToggleAction(userAction, actionTypeEnum);
				break;

			case VIDEO_COIN:
				handleCoinAction(userAction, actionTypeEnum);
				break;
		}

	}

	private void handleToggleAction(UserAction userAction, UserActionTypeEnum userActionTypeEnum)
	{
		// 因为已经加入了unique字段索引 所以插入成功表示原先不存在
		Integer insertRows = userActionMapper.insertIgnore(userAction);
		boolean liked = insertRows > 0;

		if (!liked) {
			//插入失败需要进行删除
			deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(
					userAction.getVideoId(),
					userAction.getCommentId(),
					userAction.getActionType(),
					userAction.getUserId()
			);
		}

		int count = liked ? userAction.getActionCount(): -userAction.getActionCount();
		videoInfoMapper.updateCount(userAction.getVideoId(), userActionTypeEnum.getField(), count);

		if (userActionTypeEnum.equals(UserActionTypeEnum.VIDEO_COLLECT))
		{
			//TODO 将收藏数量更新到es
		}
	}

	/**
	 * 尽量避免先查询操作, 涉及到扣减问题一定要用原子更新 或者 加锁避免并发问题
	 */
	private void handleCoinAction(UserAction userAction, UserActionTypeEnum userActionTypeEnum)
	{

		String userId = userAction.getUserId();
		String videoUserId = userAction.getVideoUserId();

		if (userId.equals(videoUserId))
			throw new BusinessException("不能给自己投币");

		// 因为已经加入了unique字段索引 所以插入成功表示原先不存在
		Integer insertRows = userActionMapper.insertIgnore(userAction);
		boolean hasNoCoinEver = insertRows > 0;

		if (!hasNoCoinEver)
			throw new BusinessException("已经投过币了");;

		//扣除投币用户硬币
		Integer updateRows = userInfoMapper.updateUserCoin(userId, -userAction.getActionCount());
		boolean isDecreaseSuccess = updateRows > 0;

		if (!isDecreaseSuccess)
		{
			throw new BusinessException("投币失败,硬币不足");
		}

		//增加发布视频用户硬币
		userInfoMapper.updateUserCoin(videoUserId, userAction.getActionCount());

		videoInfoMapper.updateCount(userAction.getVideoId(), userActionTypeEnum.getField(), userAction.getActionCount());
	}

	@Override
	public List<UserActionVO> getUserActionTypeList(UserActionQuery actionQuery) {
		return userActionMapper.selectActionTypeList(actionQuery);
	}

}
