package com.easylive.web.controller;

import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息Service
 */

@RestController
@RequestMapping("videoInfo")
public class VideoInfoController extends ABaseController {
	@Resource
	private VideoInfoService videoInfoService;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (VideoInfoQuery query) {
		return getSuccessResponseVO(videoInfoService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(VideoInfoQuery param) {
		return this.videoInfoService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param) {
		Integer count = this.videoInfoService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfo> list = this.videoInfoService.findListByParam(param);
		PaginationResultVO<VideoInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(VideoInfo bean) {
		videoInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<VideoInfo> listBean) {
		videoInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<VideoInfo> listBean) {
		videoInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 VideoId查询
	 */
	@RequestMapping("getVideoInfoByVideoId")
	public ResponseVO getVideoInfoByVideoId(String videoId) {
		return getSuccessResponseVO(this.videoInfoService.getVideoInfoByVideoId(videoId));
	}

	/**
	 * @description 根据 VideoId更新
	 */
	@RequestMapping("updateVideoInfoByVideoId")
	public ResponseVO updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
		this.videoInfoService.updateVideoInfoByVideoId(bean, videoId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 VideoId删除
	 */
	@RequestMapping("deleteVideoInfoByVideoId")
	public ResponseVO deleteVideoInfoByVideoId(String videoId) {
		this.videoInfoService.deleteVideoInfoByVideoId(videoId);
		return getSuccessResponseVO(null);
	}

}