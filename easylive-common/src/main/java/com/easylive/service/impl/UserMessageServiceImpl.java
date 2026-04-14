package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.entity.dto.UserMessageExtendDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.UserMessage;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.query.UserMessageQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.MessageNoticeVO;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.UserMessageMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserMessageService;
import com.easylive.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表Service
 */

@Service("UserMessageService")
public class UserMessageServiceImpl implements UserMessageService {
	@Resource
	private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserMessage> findListByParam(UserMessageQuery param) {
		return this.userMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserMessageQuery param) {
		return this.userMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserMessage> list = this.findListByParam(param);
		PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserMessage bean) {
		return this.userMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserMessage>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMessageMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 MessageId查询
	 */
	@Override
	public UserMessage getUserMessageByMessageId(Integer messageId) {
		return this.userMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据 MessageId更新
	 */
	@Override
	public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
		return this.userMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据 MessageId删除
	 */
	@Override
	public Integer deleteUserMessageByMessageId(Integer messageId) {
		return this.userMessageMapper.deleteByMessageId(messageId);
	}

	@Override
	public List<MessageNoticeVO> fullCompleteInfo(List<UserMessage> list, Integer messageType)
	{
		if (list == null || list.isEmpty())
			return Collections.emptyList();

		// 收件箱走的是列表页，先把当前页需要的发送人和视频信息一次性查出来，
		// 避免在循环里一条消息查一次用户、一条消息查一次视频。
		Map<String, UserInfo> sendUserMap = loadSendUserMap(list);
		Map<String, VideoInfo> videoInfoMap = loadVideoInfoMap(list);

		// 前端如果已经按消息类型筛过一遍，这里就没必要再对每条消息做一次 switch。

		if (messageType != null) {
			return buildNoticeListByMessageType(list, messageType, sendUserMap, videoInfoMap);
		}

		// 只有消息中心首页这种混合流场景，才回退到逐条判断。
		return buildMixedNoticeList(list, sendUserMap, videoInfoMap);
	}

	@Override
	public Integer getNoReadMessageCount(UserMessageQuery messageQuery) {
		return this.userMessageMapper.selectCount(messageQuery);
	}

	@Override
	public Integer updateReadStatsBatch(UserMessageQuery userMessageQuery) {
		return this.userMessageMapper.updateReadStatsBatch(userMessageQuery);
	}

	private List<MessageNoticeVO> buildNoticeListByMessageType(List<UserMessage> list, Integer messageType, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap) {
		MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnum(messageType);
		if (messageTypeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		return switch (messageTypeEnum) {
			case SYSTEM -> handleSysMessageList(list, sendUserMap, videoInfoMap);
			case LIKE, COLLECT,COMMENT -> handleActionMessageList(list, sendUserMap, videoInfoMap);
		};
	}

	private List<MessageNoticeVO> handleSysMessageList(List<UserMessage> list, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap) {
		//TODO 未完成系统通知
		return buildBaseNoticeList(list, sendUserMap, videoInfoMap);
	}

	private List<MessageNoticeVO> handleActionMessageList(List<UserMessage> list, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap) {
		return buildBaseNoticeList(list, sendUserMap, videoInfoMap);
	}

	private List<MessageNoticeVO> buildBaseNoticeList(List<UserMessage> list, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap) {
		List<MessageNoticeVO> messageNoticeVOList = new ArrayList<>(list.size());
		for (UserMessage userMessage : list) {
			messageNoticeVOList.add(buildBaseNoticeVO(userMessage, sendUserMap, videoInfoMap));
		}
		return messageNoticeVOList;
	}

	private MessageNoticeVO buildBaseNoticeVO(UserMessage userMessage, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap) {
		MessageNoticeVO messageNoticeVO = BeanUtil.toBean(userMessage, MessageNoticeVO.class);

		// extendJson 里放的是这条消息自己的补充信息，比如 commentId、actionType、内容摘要这些。
		UserMessageExtendDTO extendDTO = parseExtendDTO(userMessage.getExtendJson());
		messageNoticeVO.setExtendDto(extendDTO);

		// 某些消息 videoId 会直接落在主表，某些消息会放在 extendJson，这里统一兜底一次。
		String videoId = extendDTO.getVideoId() == null ? userMessage.getVideoId() : extendDTO.getVideoId();
		messageNoticeVO.setVideoId(videoId);

		// 发送人信息优先从当前页批量查出来的 map 里取，少走数据库。
		UserInfo sendUser = sendUserMap.get(userMessage.getSendUserId());
		if (sendUser != null) {
			messageNoticeVO.setSendUserName(sendUser.getNickName());
			messageNoticeVO.setSendUserAvatar(sendUser.getAvatar());
		}

		// 消息列表这里只需要封面，不把整条视频详情都塞给前端。
		VideoInfo videoInfo = videoInfoMap.get(videoId);
		if (videoInfo != null) {
			messageNoticeVO.setVideoCover(videoInfo.getVideoCover());
		}
		return messageNoticeVO;
	}

	private UserMessageExtendDTO parseExtendDTO(String extendJson) {
		if (extendJson == null || extendJson.trim().isEmpty()) {
			return new UserMessageExtendDTO();
		}
		UserMessageExtendDTO extendDTO = JsonUtils.convertJson2Obj(extendJson, UserMessageExtendDTO.class);
		return extendDTO == null ? new UserMessageExtendDTO() : extendDTO;
	}

	private Map<String, UserInfo> loadSendUserMap(List<UserMessage> list) {
		Set<String> sendUserIdSet = new LinkedHashSet<>();
		for (UserMessage userMessage : list) {
			if (userMessage.getSendUserId() != null && !userMessage.getSendUserId().trim().isEmpty()) {
				sendUserIdSet.add(userMessage.getSendUserId());
			}
		}
		if (sendUserIdSet.isEmpty()) {
			return Collections.emptyMap();
		}

		// 这里只查当前页消息里真正出现过的发送人，查询范围会更小。
		UserInfoQuery userInfoQuery = new UserInfoQuery();
		userInfoQuery.setUserIds(new ArrayList<>(sendUserIdSet));
		List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);
		Map<String, UserInfo> userInfoMap = new HashMap<>();
		for (UserInfo userInfo : userInfoList) {
			userInfoMap.put(userInfo.getUserId(), userInfo);
		}
		return userInfoMap;
	}

	private Map<String, VideoInfo> loadVideoInfoMap(List<UserMessage> list) {
		Set<String> videoIdSet = new LinkedHashSet<>();
		for (UserMessage userMessage : list) {
			UserMessageExtendDTO extendDTO = parseExtendDTO(userMessage.getExtendJson());
			String videoId = extendDTO.getVideoId() == null ? userMessage.getVideoId() : extendDTO.getVideoId();
			if (videoId != null && !videoId.trim().isEmpty()) {
				videoIdSet.add(videoId);
			}
		}
		if (videoIdSet.isEmpty()) {
			return Collections.emptyMap();
		}

		// 视频信息同样只按当前页涉及到的 videoId 批量查，避免做无意义的全量关联。
		List<VideoInfo> videoInfoList = videoInfoMapper.selectByIds(new ArrayList<>(videoIdSet));
		Map<String, VideoInfo> videoInfoMap = new HashMap<>();
		for (VideoInfo videoInfo : videoInfoList) {
			videoInfoMap.put(videoInfo.getVideoId(), videoInfo);
		}
		return videoInfoMap;
	}


	private List<MessageNoticeVO> buildMixedNoticeList(List<UserMessage> list, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap) {
		List<MessageNoticeVO> messageNoticeVOList = new ArrayList<>(list.size());
		for (UserMessage userMessage : list)
		{
			MessageNoticeVO messageNoticeVO = null;

			switch (MessageTypeEnum.getEnum(userMessage.getMessageType()))
			{
				case MessageTypeEnum.SYSTEM ->{
					// 系统消息暂时复用统一的展示模型，后面真要做平台公告，再单独拆展示逻辑。
					messageNoticeVO = handleSysMessage(userMessage, sendUserMap, videoInfoMap);
				}
				case MessageTypeEnum.LIKE,MessageTypeEnum.COLLECT, MessageTypeEnum.COMMENT -> {
					// 点赞和收藏在列表展示上差异不大，先走同一条组装逻辑，文案层再区分。
					messageNoticeVO = handleActionMessage(userMessage, sendUserMap, videoInfoMap);
				}
				case null, default -> throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			messageNoticeVOList.add(messageNoticeVO);
		}
		return messageNoticeVOList;
	}

	private MessageNoticeVO handleSysMessage(UserMessage userMessage, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap)
	{
		return buildBaseNoticeVO(userMessage, sendUserMap, videoInfoMap);
	}

	private MessageNoticeVO handleActionMessage(UserMessage userMessage, Map<String, UserInfo> sendUserMap, Map<String, VideoInfo> videoInfoMap)
	{
		return buildBaseNoticeVO(userMessage, sendUserMap, videoInfoMap);
	}


}
