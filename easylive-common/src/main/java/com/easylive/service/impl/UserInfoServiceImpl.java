package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.RegisterDTO;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.WebLoginDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.enums.SexEnum;
import com.easylive.enums.StatusEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.service.UserInfoService;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


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
	private RedisComponent redisComponent;
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

		//TODO 设置硬币数量
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

		return tokenUserInfoDTO;
	}
}
