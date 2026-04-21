package com.easylive.service;

import com.easylive.entity.dto.RegisterDTO;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.WebLoginDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.*;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.exception.BusinessException;

import java.util.List;


/**
 * @author amani
 * @since 2026/01/07
 */

public interface UserInfoService {

	/**
	 * @description 根据条件查询
	 */
	List<UserInfo> findListByParam(UserInfoQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(UserInfoQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param);

	/**
	 * @description 新增
	 */
	Integer add(UserInfo bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<UserInfo>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserInfo> listBean);


	/**
	 * @description 根据 UserId查询
	 */
	UserInfo getUserInfoByUserId(String userId);

	/**
	 * @description 根据 UserId更新
	 */
	Integer updateUserInfoByUserId(UserInfo bean, String userId);

	/**
	 * @description 根据 UserId删除
	 */
	Integer deleteUserInfoByUserId(String userId);


	/**
	 * @description 根据 Email查询
	 */
	UserInfo getUserInfoByEmail(String email);

	/**
	 * @description 根据 Email更新
	 */
	Integer updateUserInfoByEmail(UserInfo bean, String email);

	/**
	 * @description 根据 Email删除
	 */
	Integer deleteUserInfoByEmail(String email);


	/**
	 * @description 根据 NickName查询
	 */
	UserInfo getUserInfoByNickName(String nickName);

	/**
	 * @description 根据 NickName更新
	 */
	Integer updateUserInfoByNickName(UserInfo bean, String nickName);

	/**
	 * @description 根据 NickName删除
	 */
	Integer deleteUserInfoByNickName(String nickName);

	/**
	 * 用户注册方法
	 * @param registerDTO 注册数据传输对象，包含用户注册所需的所有信息
	 * @throws BusinessException 当注册过程中出现业务异常时抛出，如用户名已存在、验证码错误等
	 */
	void register(RegisterDTO registerDTO);

	/**
	 * 用户登录方法
	 * @param webLoginDTO 登录数据传输对象，包含用户登录所需的信息
	 */
	TokenUserInfoDTO login(WebLoginDTO webLoginDTO);

	void setUserInHome(UserInfoVO userInfoVO);

	PaginationResultVO<VideoInfoUHomeVO> loadUHomeVideoList(String userId, Integer type, Integer pageNo, String videoName, Integer orderType);

	UserInfoVO getUHomeUserInfo(String userId, TokenUserInfoDTO currentUser);

	void updateUserInfoUHome(TokenUserInfoDTO tokenUserInfoDTO, UserInfo userInfo);

	void saveTheme(String userId, Integer theme);

	/**
	 * 查询硬币数量
	 */
	Integer selectTotalCoinCount(String userId);


	UserCountVO getUserCountInfo(String userId);

    UCenterVideoDateVO getActualTimeStatisticsInfo(String userId);

    List<UCenterVideoWeekCountVO> getWeekStatisticsInfo(UserStatsRedisEnum anEnum, String userId);

	Integer changeStatus(String userId, Integer type);
}
