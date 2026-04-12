package com.easylive.web.controller;

import com.easylive.entity.po.UserMessage;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserMessageQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.UserMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表Service
 */

@RestController
@RequestMapping("userMessage")
public class UserMessageController extends ABaseController {
	@Resource
	private UserMessageService userMessageService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (UserMessageQuery query) {
		return getSuccessResponseVO(userMessageService.findListByPage(query));
	}

	/**
	 * 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(UserMessageQuery param) {
		return this.userMessageService.findCountByParam(param);
	}

	/**
	 * 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
		Integer count = this.userMessageService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserMessage> list = this.userMessageService.findListByParam(param);
		PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(UserMessage bean) {
		userMessageService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<UserMessage> listBean) {
		userMessageService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserMessage> listBean) {
		userMessageService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * 根据 MessageId查询
	 */
	@RequestMapping("getUserMessageByMessageId")
	public ResponseVO getUserMessageByMessageId(Integer messageId) {
		return getSuccessResponseVO(this.userMessageService.getUserMessageByMessageId(messageId));
	}

	/**
	 * 根据 MessageId更新
	 */
	@RequestMapping("updateUserMessageByMessageId")
	public ResponseVO updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
		this.userMessageService.updateUserMessageByMessageId(bean, messageId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据 MessageId删除
	 */
	@RequestMapping("deleteUserMessageByMessageId")
	public ResponseVO deleteUserMessageByMessageId(Integer messageId) {
		this.userMessageService.deleteUserMessageByMessageId(messageId);
		return getSuccessResponseVO(null);
	}

}