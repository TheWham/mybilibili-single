package com.easylive.web.controller;

import com.easylive.entity.po.VideoPlayHistory;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoPlayHistoryQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.VideoPlayHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史Service
 */

@RestController
@RequestMapping("videoPlayHistory")
public class VideoPlayHistoryController extends ABaseController {
	@Resource
	private VideoPlayHistoryService videoPlayHistoryService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (VideoPlayHistoryQuery query) {
		return getSuccessResponseVO(videoPlayHistoryService.findListByPage(query));
	}

	/**
	 * 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(VideoPlayHistoryQuery param) {
		return this.videoPlayHistoryService.findCountByParam(param);
	}

	/**
	 * 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery param) {
		Integer count = this.videoPlayHistoryService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoPlayHistory> list = this.videoPlayHistoryService.findListByParam(param);
		PaginationResultVO<VideoPlayHistory> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(VideoPlayHistory bean) {
		videoPlayHistoryService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<VideoPlayHistory> listBean) {
		videoPlayHistoryService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<VideoPlayHistory> listBean) {
		videoPlayHistoryService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * 根据 UserIdAndVideoId查询
	 */
	@RequestMapping("getVideoPlayHistoryByUserIdAndVideoId")
	public ResponseVO getVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId) {
		return getSuccessResponseVO(this.videoPlayHistoryService.getVideoPlayHistoryByUserIdAndVideoId(userId, videoId));
	}

	/**
	 * 根据 UserIdAndVideoId更新
	 */
	@RequestMapping("updateVideoPlayHistoryByUserIdAndVideoId")
	public ResponseVO updateVideoPlayHistoryByUserIdAndVideoId(VideoPlayHistory bean, String userId, String videoId) {
		this.videoPlayHistoryService.updateVideoPlayHistoryByUserIdAndVideoId(bean, userId, videoId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据 UserIdAndVideoId删除
	 */
	@RequestMapping("deleteVideoPlayHistoryByUserIdAndVideoId")
	public ResponseVO deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId) {
		this.videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(userId, videoId);
		return getSuccessResponseVO(null);
	}

}