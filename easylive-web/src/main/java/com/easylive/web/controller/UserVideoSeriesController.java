package com.easylive.web.controller;

import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.UserVideoSeriesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * @author amani
 * @since 2026/03/18
 * 用户视频列表Service
 */

@RestController
@RequestMapping("userVideoSeries")
public class UserVideoSeriesController extends ABaseController {
	@Resource
	private UserVideoSeriesService userVideoSeriesService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (UserVideoSeriesQuery query) {
		return getSuccessResponseVO(userVideoSeriesService.findListByPage(query));
	}

	/**
	 * 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(UserVideoSeriesQuery param) {
		return this.userVideoSeriesService.findCountByParam(param);
	}

	/**
	 * 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param) {
		Integer count = this.userVideoSeriesService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoSeries> list = this.userVideoSeriesService.findListByParam(param);
		PaginationResultVO<UserVideoSeries> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(UserVideoSeries bean) {
		userVideoSeriesService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<UserVideoSeries> listBean) {
		userVideoSeriesService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserVideoSeries> listBean) {
		userVideoSeriesService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * 根据 SeriesId查询
	 */
	@RequestMapping("getUserVideoSeriesBySeriesId")
	public ResponseVO getUserVideoSeriesBySeriesId(Integer seriesId) {
		return getSuccessResponseVO(this.userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId));
	}

	/**
	 * 根据 SeriesId更新
	 */
	@RequestMapping("updateUserVideoSeriesBySeriesId")
	public ResponseVO updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId) {
		this.userVideoSeriesService.updateUserVideoSeriesBySeriesId(bean, seriesId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据 SeriesId删除
	 */
	@RequestMapping("deleteUserVideoSeriesBySeriesId")
	public ResponseVO deleteUserVideoSeriesBySeriesId(Integer seriesId) {
		this.userVideoSeriesService.deleteUserVideoSeriesBySeriesId(seriesId);
		return getSuccessResponseVO(null);
	}

}