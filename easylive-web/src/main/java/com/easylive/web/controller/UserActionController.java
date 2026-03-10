package com.easylive.web.controller;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.entity.dto.UserActionDTO;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.UserActionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论Service
 */

@RestController
@RequestMapping("userAction")
public class UserActionController extends ABaseController {
	@Resource
	private UserActionService userActionService;


	@RequestMapping("/doAction")
	public ResponseVO doAction(@Validated UserActionDTO userActionDTO)
	{
		UserAction userAction = BeanUtil.toBean(userActionDTO, UserAction.class);
		userAction.setUserId(getTokenUserInfo().getUserId());
		userAction.setActionTime(new Date());
		userAction.setActionCount(userActionDTO.getActionCount() == null ? 1: userActionDTO.getActionCount());
		userActionService.doAction(userAction);
		return getSuccessResponseVO(null);
	}
}
