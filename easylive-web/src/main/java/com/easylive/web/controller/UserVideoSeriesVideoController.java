package com.easylive.web.controller;

import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.UserVideoSeriesVideoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频Service
 */

@RestController
@RequestMapping("userVideoSeriesVideo")
public class UserVideoSeriesVideoController extends ABaseController {
	@Resource
	private UserVideoSeriesVideoService userVideoSeriesVideoService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (UserVideoSeriesVideoQuery query) {
		return getSuccessResponseVO(userVideoSeriesVideoService.findListByPage(query));
	}

	/**
	 * 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(UserVideoSeriesVideoQuery param) {
		return this.userVideoSeriesVideoService.findCountByParam(param);
	}

	/**
	 * 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<UserVideoSeriesVideo> findListByPage(UserVideoSeriesVideoQuery param) {
		Integer count = this.userVideoSeriesVideoService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoSeriesVideo> list = this.userVideoSeriesVideoService.findListByParam(param);
		PaginationResultVO<UserVideoSeriesVideo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(UserVideoSeriesVideo bean) {
		userVideoSeriesVideoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<UserVideoSeriesVideo> listBean) {
		userVideoSeriesVideoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserVideoSeriesVideo> listBean) {
		userVideoSeriesVideoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * 根据 SeriesIdAndVideoId查询
	 */
	@RequestMapping("getUserVideoSeriesVideoBySeriesIdAndVideoId")
	public ResponseVO getUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId) {
		return getSuccessResponseVO(this.userVideoSeriesVideoService.getUserVideoSeriesVideoBySeriesIdAndVideoId(seriesId, videoId));
	}

	/**
	 * 根据 SeriesIdAndVideoId更新
	 */
	@RequestMapping("updateUserVideoSeriesVideoBySeriesIdAndVideoId")
	public ResponseVO updateUserVideoSeriesVideoBySeriesIdAndVideoId(UserVideoSeriesVideo bean, Integer seriesId, String videoId) {
		this.userVideoSeriesVideoService.updateUserVideoSeriesVideoBySeriesIdAndVideoId(bean, seriesId, videoId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据 SeriesIdAndVideoId删除
	 */
	@RequestMapping("deleteUserVideoSeriesVideoBySeriesIdAndVideoId")
	public ResponseVO deleteUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId) {
		this.userVideoSeriesVideoService.deleteUserVideoSeriesVideoBySeriesIdAndVideoId(seriesId, videoId);
		return getSuccessResponseVO(null);
	}

}