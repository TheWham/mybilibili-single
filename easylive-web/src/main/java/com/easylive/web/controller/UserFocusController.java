package com.easylive.web.controller;

import com.easylive.entity.po.UserFocus;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.UserFocusService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表Service
 */

@RestController
@RequestMapping("userFocus")
public class UserFocusController extends ABaseController {
	@Resource
	private UserFocusService userFocusService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (UserFocusQuery query) {
		return getSuccessResponseVO(userFocusService.findListByPage(query));
	}

	/**
	 * 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(UserFocusQuery param) {
		return this.userFocusService.findCountByParam(param);
	}

	/**
	 * 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param) {
		Integer count = this.userFocusService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserFocus> list = this.userFocusService.findListByParam(param);
		PaginationResultVO<UserFocus> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(UserFocus bean) {
		userFocusService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<UserFocus> listBean) {
		userFocusService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserFocus> listBean) {
		userFocusService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * 根据 UserIdAndUserFocusId查询
	 */
	@RequestMapping("getUserFocusByUserIdAndUserFocusId")
	public ResponseVO getUserFocusByUserIdAndUserFocusId(String userId, String userFocusId) {
		return getSuccessResponseVO(this.userFocusService.getUserFocusByUserIdAndUserFocusId(userId, userFocusId));
	}

	/**
	 * 根据 UserIdAndUserFocusId更新
	 */
	@RequestMapping("updateUserFocusByUserIdAndUserFocusId")
	public ResponseVO updateUserFocusByUserIdAndUserFocusId(UserFocus bean, String userId, String userFocusId) {
		this.userFocusService.updateUserFocusByUserIdAndUserFocusId(bean, userId, userFocusId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据 UserIdAndUserFocusId删除
	 */
	@RequestMapping("deleteUserFocusByUserIdAndUserFocusId")
	public ResponseVO deleteUserFocusByUserIdAndUserFocusId(String userId, String userFocusId) {
		this.userFocusService.deleteUserFocusByUserIdAndUserFocusId(userId, userFocusId);
		return getSuccessResponseVO(null);
	}

}