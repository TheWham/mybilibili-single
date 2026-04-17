package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.component.UserStatsCacheAsyncComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.RegisterDTO;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.VideoCountDTO;
import com.easylive.entity.dto.WebLoginDTO;
import com.easylive.entity.po.UserFocus;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.UserStats;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.*;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserFocusMapper;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.UserStatsMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.*;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author amani
 * @date 2026/01/07
 * @description Service
 */

@Slf4j
@Service("UserInfoService")
public class UserInfoServiceImpl implements UserInfoService {
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private VideoInfoService videoInfoService;
	@Resource
	private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
	@Resource
	private RedisComponent redisComponent;
	@Resource
	private UserStatsCacheAsyncComponent userStatsCacheAsyncComponent;
	@Resource
	private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;
	@Resource
	private UserFocusService userFocusService;
	@Resource
	private UserVideoActionService userVideoActionService;
	@Resource
	private VideoCommentService videoCommentService;
	@Resource
	private VideoDanmuService videoDanmuService;
	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 UserId查询
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * @description 根据 UserId更新
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * @description 根据 UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}


	/**
	 * @description 根据 Email查询
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * @description 根据 Email更新
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * @description 根据 Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}


	/**
	 * @description 根据 NickName查询
	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName) {
		return this.userInfoMapper.selectByNickName(nickName);
	}

	/**
	 * @description 根据 NickName更新
	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
		return this.userInfoMapper.updateByNickName(bean, nickName);
	}

	/**
	 * @description 根据 NickName删除
	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName) {
		return this.userInfoMapper.deleteByNickName(nickName);
	}

	@Override
	public void register(RegisterDTO registerDTO) {
		String email = registerDTO.getEmail();
		String nickName = registerDTO.getNickName();
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (userInfo != null)
			throw new BusinessException("邮箱已经存在");

		UserInfo userInfo1 = this.userInfoMapper.selectByNickName(nickName);

		if (userInfo1 != null)
			throw new BusinessException("昵称已经存在");

		UserInfo aUserInfo = BeanUtil.toBean(registerDTO, UserInfo.class);
		aUserInfo.setUserId(StringTools.generateRandomNumber(Constants.USER_ID_LENGTH));
		aUserInfo.setPassword(StringTools.md5Password(registerDTO.getRegisterPassword()));
		aUserInfo.setJoinTime(new Date());
		aUserInfo.setStatus(StatusEnum.NORMAL.getType());
		aUserInfo.setSex(SexEnum.UNKNOWN.getType());

		aUserInfo.setCurrentCoinCount(Constants.DEFAULT_COIN_COUNT);
		aUserInfo.setTotalCoinCount(Constants.DEFAULT_COIN_COUNT);
		this.userInfoMapper.insert(aUserInfo);

	}

	@Override
	public TokenUserInfoDTO login(WebLoginDTO webLoginDTO) {
		String email = webLoginDTO.getEmail();
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		//前端以作MD5校验
		if (userInfo == null ||!userInfo.getPassword().equals(webLoginDTO.getPassword()))
			throw new BusinessException("账号或密码错误");

		if (StatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())){
			throw new BusinessException("用户已被禁用");
		}

		userInfo.setLastLoginIp(webLoginDTO.getLastLoginIp());
		userInfo.setLastLoginTime(new Date());
		//更新登录信息
		this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

		TokenUserInfoDTO tokenUserInfoDTO = BeanUtil.toBean(userInfo, TokenUserInfoDTO.class);

		// 将登录信息存放到redis,并返回前端可以存取tokenId并获得userInfo
		redisComponent.saveTokenUserInfo(tokenUserInfoDTO);
		String userId = userInfo.getUserId();
		String tokenId = redisComponent.getTokenIdByUserId(userId);

		if (tokenId != null) {
			redisComponent.cleanExistToken(userId);
		}
		redisComponent.saveTokenIdByUserId(userInfo.getUserId(), tokenUserInfoDTO.getTokenId());
		//刷新用户统计数量缓存
		redisComponent.refreshRealtimeUserStatsExpire(userInfo.getUserId());
		userStatsCacheAsyncComponent.refreshRealtimeUserStatsCache(userInfo.getUserId());
		return tokenUserInfoDTO;
	}

	@Override
	public void setUserInHome(UserInfoVO userInfoVO) {
		String userId = userInfoVO.getUserId();
		HashMap<String, Integer> realtimeStatsMap = redisComponent.getRealtimeUserStatsInfo(userId);
		if (realtimeStatsMap != null && !realtimeStatsMap.isEmpty()) {
			redisComponent.refreshRealtimeUserStatsExpire(userId);
			fillUserInfoVOWithRealtimeStats(userInfoVO, realtimeStatsMap);
			return;
		}

		// Redis 没命中时再退回最近一天的统计，避免 user_stats 多天数据直接查炸。
		UserStats userStats = userStatsMapper.selectLatestByUserId(userId);
		if (userStats == null) {
			userInfoVO.setPlayCount(0);
			userInfoVO.setLikeCount(0);
			userInfoVO.setFansCount(0);
			userInfoVO.setFocusCount(0);
			return;
		}
		userInfoVO.setPlayCount(Optional.ofNullable(userStats.getPlayCount()).orElse(0));
		userInfoVO.setLikeCount(Optional.ofNullable(userStats.getLikeCount()).orElse(0));
		userInfoVO.setFansCount(Optional.ofNullable(userStats.getFansCount()).orElse(0));
		userInfoVO.setFocusCount(Optional.ofNullable(userStats.getFocusCount()).orElse(0));
	}

	@Override
	public PaginationResultVO<VideoInfoUHomeVO> loadUHomeVideoList(String userId, Integer type, Integer pageNo, String videoName, Integer orderType) {
		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		videoInfoQuery.setUserId(userId);
		videoInfoQuery.setPageNo(pageNo);
		videoInfoQuery.setVideoName(videoName);
		if (type != null) {
			videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());
		}

		VideoOrderTypeEnum typeEnum = VideoOrderTypeEnum.getEnum(orderType);
		if (typeEnum == null) {
			typeEnum = VideoOrderTypeEnum.ORDER_POST_TIME;
		}
		videoInfoQuery.setOrderBy(typeEnum.getField() + " desc");

		PaginationResultVO<VideoInfo> listVideo = videoInfoService.findListByPage(videoInfoQuery);
		PaginationResultVO<VideoInfoUHomeVO> videoListVO = new PaginationResultVO<>();
		videoListVO.setPageNo(listVideo.getPageNo());
		videoListVO.setPageSize(listVideo.getPageSize());
		videoListVO.setPageTotal(listVideo.getPageTotal());
		videoListVO.setTotalCount(listVideo.getTotalCount());
		videoListVO.setList(BeanUtil.copyToList(listVideo.getList(), VideoInfoUHomeVO.class));
		return videoListVO;
	}

	@Override
	public UserInfoVO getUHomeUserInfo(String userId, TokenUserInfoDTO currentUser) {
		UserInfo userInfoDb = this.getUserInfoByUserId(userId);
		if (userInfoDb == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		UserInfoVO userInfoVO = BeanUtil.toBean(userInfoDb, UserInfoVO.class);
		if (currentUser != null && !userId.equals(currentUser.getUserId())) {
			Integer haveFocus = userFocusService.selectHaveFocus(currentUser.getUserId(), userId);
			userInfoVO.setHaveFocus(haveFocus);
		}
		this.setUserInHome(userInfoVO);
		return userInfoVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserInfoUHome(TokenUserInfoDTO tokenUserInfoDTO, UserInfo userInfo) {
		String userId = tokenUserInfoDTO.getUserId();
		boolean isEditNickName = !tokenUserInfoDTO.getNickName().equals(userInfo.getNickName());
		//修改名称需要扣硬币
		Integer totalCoinCount = userInfoMapper.selectTotalCoinCount(userId);
		if (isEditNickName && totalCoinCount < Constants.UPDATE_NAME_COIN)
		{
			throw new BusinessException("硬币不足, 无法修改昵称");
		}
		userInfoMapper.updateByUserId(userInfo, userId);

		//更新硬币数量
		if (isEditNickName){
			Integer count = userInfoMapper.updateUserCoin(userId, -Constants.UPDATE_NAME_COIN);
			if (count == 0){
				throw new BusinessException("硬币不足,无法修改昵称");
			}
		}

		boolean isEditAvatar = tokenUserInfoDTO.getAvatar() == null || !tokenUserInfoDTO.getAvatar().equals(userInfo.getAvatar());
		boolean isEditPersonIntroduction = tokenUserInfoDTO.getPersonIntroduction() == null || !tokenUserInfoDTO.getPersonIntroduction().equals(userInfo.getPersonIntroduction());
		if (isEditNickName|| isEditAvatar || isEditPersonIntroduction)
		{
			tokenUserInfoDTO.setNickName(userInfo.getNickName());
			tokenUserInfoDTO.setPersonIntroduction(userInfo.getPersonIntroduction());
			tokenUserInfoDTO.setAvatar(userInfo.getAvatar());
			redisComponent.updateTokenUserInfo(tokenUserInfoDTO);
		}

	}

	@Override
	public void saveTheme(String userId, Integer theme) {
		UserInfo userInfo = new UserInfo();
		userInfo.setTheme(theme);
		this.updateUserInfoByUserId(userInfo, userId);
	}

	@Override
	public Integer selectTotalCoinCount(String userId) {
		return this.selectTotalCoinCount(userId);
	}

	@Override
	public UserCountVO getUserCountInfo(String userId) {
		HashMap<String, Integer> userStatsMap = redisComponent.getRealtimeUserStatsInfo(userId);
		if (userStatsMap != null && !userStatsMap.isEmpty()) {
			redisComponent.refreshRealtimeUserStatsExpire(userId);
			return buildUserCountVO(userStatsMap);
		}

		UserInfo userInfo = userInfoMapper.selectByUserId(userId);
		if (userInfo == null) {
			return null;
		}
		//异步刷新缓存
		userStatsCacheAsyncComponent.refreshRealtimeUserStatsCache(userId);
		return buildUserCountVO(userId, userInfo);
	}

	private UserCountVO buildUserCountVO(HashMap<String, Integer> userStatsMap) {
		UserCountVO userCountVO = new UserCountVO();
		userCountVO.setFocusCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FOCUS.getField(), 0));
		userCountVO.setFansCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
		userCountVO.setCurrentCoinCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_COIN.getField(), 0));
		userCountVO.setLikeCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
		userCountVO.setPlayCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
		return userCountVO;
	}

	private UserCountVO buildUserCountVO(String userId, UserInfo userInfo) {
		UserCountVO userCountVO = new UserCountVO();
		userCountVO.setCurrentCoinCount(Optional.ofNullable(userInfo.getCurrentCoinCount()).orElse(0));

		UserFocusQuery focusQuery = new UserFocusQuery();
		focusQuery.setUserId(userId);
		userCountVO.setFocusCount(Optional.ofNullable(userFocusMapper.selectCount(focusQuery)).orElse(0));

		UserFocusQuery fansQuery = new UserFocusQuery();
		fansQuery.setUserFocusId(userId);
		userCountVO.setFansCount(Optional.ofNullable(userFocusMapper.selectCount(fansQuery)).orElse(0));

		VideoCountDTO videoCountDTO = videoInfoMapper.sumVideoCountByUserId(userId);
		if (videoCountDTO == null) {
			userCountVO.setLikeCount(0);
			userCountVO.setPlayCount(0);
			return userCountVO;
		}
		userCountVO.setLikeCount(Optional.ofNullable(videoCountDTO.getTotalLikeCount()).orElse(0));
		userCountVO.setPlayCount(Optional.ofNullable(videoCountDTO.getTotalPlayCount()).orElse(0));
		return userCountVO;
	}

	private void fillUserInfoVOWithRealtimeStats(UserInfoVO userInfoVO, HashMap<String, Integer> userStatsMap) {
		userInfoVO.setFocusCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FOCUS.getField(), 0));
		userInfoVO.setFansCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
		userInfoVO.setLikeCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
		userInfoVO.setPlayCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
	}

	@Override
	public UCenterVideoDateVO getActualTimeStatisticsInfo(String userId) {
		UserInfo userInfo = userInfoMapper.selectByUserId(userId);
		Optional.ofNullable(userInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

		// 这个接口前端是拿来展示“当前实时数据”的，点赞/投币后刷新页面也应该立刻看到变化。
		// 所以这里优先读实时统计缓存，而不是读按天快照；按天数据更适合做昨日对比和周趋势。
		HashMap<String, Integer> userStatsInfoMap = redisComponent.getRealtimeUserStatsInfo(userId);
		UCenterVideoDateVO uCenterVideoDateVO = new UCenterVideoDateVO();
		TotalCountInfoVO totalCountInfoVO = new TotalCountInfoVO();
        uCenterVideoDateVO.setTotalCountInfo(totalCountInfoVO);
		uCenterVideoDateVO.setPreDayData(buildPreDayData(userId));

		if (userStatsInfoMap != null && !userStatsInfoMap.isEmpty())
		{
			redisComponent.refreshRealtimeUserStatsExpire(userId);
			fillTotalCountInfo(totalCountInfoVO, userStatsInfoMap);
			return uCenterVideoDateVO;
		}
		//redis没有走mysql
		uCenterVideoDateVO.setTotalCountInfo(fillTotalCountInfoInDB(userId));
		return uCenterVideoDateVO;
	}

	private void fillTotalCountInfo(TotalCountInfoVO totalCountInfoVO, HashMap<String, Integer> userStatsInfoMap)
	{
		totalCountInfoVO.setCoinCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.VIDEO_COIN.getField(), 0));
		totalCountInfoVO.setCommentCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), 0));
		totalCountInfoVO.setLikeCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
		totalCountInfoVO.setCollectCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), 0));
		totalCountInfoVO.setDanmuCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.VIDEO_DANMU.getField(), 0));
		totalCountInfoVO.setPlayCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
		totalCountInfoVO.setFansCount(userStatsInfoMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
	}

	private TotalCountInfoVO fillTotalCountInfoInDB(String userId)
	{
		TotalCountInfoVO totalCountInfoVO = new TotalCountInfoVO(0, 0, 0, 0, 0, 0, 0);
        UserStats userStats = userStatsMapper.selectLatestByUserId(userId);
		if (userStats != null) {
			totalCountInfoVO = BeanUtil.toBean(userStats, TotalCountInfoVO.class);
		}
		return totalCountInfoVO;
	}

	private Integer[] buildPreDayData(String userId) {
		int dataTypeLength = UserStatsRedisEnum.values().length;
		Integer[] preDayData = new Integer[dataTypeLength];
		for (int i = 0; i < dataTypeLength; i++) {
			preDayData[i] = 0;
		}

		LocalDate preDay = LocalDate.now().minusDays(1);
		UserStatsQuery userStatsQuery = new UserStatsQuery();
		userStatsQuery.setUserId(userId);
		userStatsQuery.setStatsDay(Date.from(preDay.atStartOfDay(ZoneId.systemDefault()).toInstant()));

		// 这里只取昨天那一天的统计快照。
		// 前端拿到的 preDayData 会按 dataType 下标取值，所以这里直接一次性整理成数组。
		List<UserStats> userStatsList = userStatsMapper.selectList(userStatsQuery);
		if (userStatsList == null || userStatsList.isEmpty()) {
			return preDayData;
		}
		UserStats userStats = userStatsList.get(0);
		for (UserStatsRedisEnum statsEnum : UserStatsRedisEnum.values()) {
			preDayData[statsEnum.getType()] = getStatsCountByType(userStats, statsEnum);
		}
		return preDayData;
	}

	@Override
	public List<UCenterVideoWeekCountVO> getWeekStatisticsInfo(UserStatsRedisEnum anEnum, String userId) {
		UserInfo userInfo = userInfoMapper.selectByUserId(userId);
		Optional.ofNullable(userInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

		UserStatsQuery userStatsQuery = new UserStatsQuery();
		userStatsQuery.setUserId(userId);
		userStatsQuery.setPageNo(1);
		userStatsQuery.setPageSize(7);
		userStatsQuery.setOrderBy("v.stats_day desc");
		List<UserStats> userStatsList = userStatsMapper.selectList(userStatsQuery);

		// user_stats 里未必每天都有一条数据。
		// 这里先把库里查出来的结果按日期收成 Map，后面再把最近 7 天补齐，前端画折线图时就不会缺点位。
		Map<LocalDate, Integer> countMap = userStatsList.stream()
				.filter(userStats -> userStats.getStatsDay() != null)
				.collect(Collectors.toMap(userStats -> userStats.getStatsDay().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
						userStats -> getStatsCountByType(userStats, anEnum),
						(oldValue, newValue) -> newValue));

		List<UCenterVideoWeekCountVO> result = new ArrayList<>(7);
		LocalDate today = LocalDate.now();
		for (int i = 6; i >= 0; i--) {
			LocalDate statisticsDate = today.minusDays(i);
			UCenterVideoWeekCountVO weekCountVO = new UCenterVideoWeekCountVO();
			weekCountVO.setStatisticsDate(Date.from(statisticsDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			weekCountVO.setStatisticsCount(countMap.getOrDefault(statisticsDate, 0));
			result.add(weekCountVO);
		}
		return result;
	}

	private Integer getStatsCountByType(UserStats userStats, UserStatsRedisEnum statsType) {
		if (userStats == null || statsType == null) {
			return 0;
		}
		// 这里直接按枚举映射 user_stats 的字段，周趋势只关心某一种统计项的每日数量。
		return switch (statsType) {
			case VIDEO_LIKE -> Optional.ofNullable(userStats.getLikeCount()).orElse(0);
			case VIDEO_PLAY -> Optional.ofNullable(userStats.getPlayCount()).orElse(0);
			case VIDEO_DANMU -> Optional.ofNullable(userStats.getDanmuCount()).orElse(0);
			case VIDEO_COIN -> Optional.ofNullable(userStats.getCoinCount()).orElse(0);
			case USER_FOCUS -> Optional.ofNullable(userStats.getFocusCount()).orElse(0);
			case USER_FANS -> Optional.ofNullable(userStats.getFansCount()).orElse(0);
			case USER_COIN -> Optional.ofNullable(userStats.getCurrentCoinCount()).orElse(0);
			case USER_COMMENT_COUNT -> Optional.ofNullable(userStats.getCommentCount()).orElse(0);
			case USER_COLLECT_COUNT -> Optional.ofNullable(userStats.getCollectCount()).orElse(0);
		};
	}


}
