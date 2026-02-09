package com.easylive.web.controller;

import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoInfoFileQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.VideoInfoFileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息Service
 */

@RestController
@RequestMapping("videoInfoFile")
public class VideoInfoFileController extends ABaseController {
	@Resource
	private VideoInfoFileService videoInfoFileService;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (VideoInfoFileQuery query) {
		return getSuccessResponseVO(videoInfoFileService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(VideoInfoFileQuery param) {
		return this.videoInfoFileService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<VideoInfoFile> findListByPage(VideoInfoFileQuery param) {
		Integer count = this.videoInfoFileService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoFile> list = this.videoInfoFileService.findListByParam(param);
		PaginationResultVO<VideoInfoFile> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(VideoInfoFile bean) {
		videoInfoFileService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<VideoInfoFile> listBean) {
		videoInfoFileService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<VideoInfoFile> listBean) {
		videoInfoFileService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 FileId查询
	 */
	@RequestMapping("getVideoInfoFileByFileId")
	public ResponseVO getVideoInfoFileByFileId(String fileId) {
		return getSuccessResponseVO(this.videoInfoFileService.getVideoInfoFileByFileId(fileId));
	}

	/**
	 * @description 根据 FileId更新
	 */
	@RequestMapping("updateVideoInfoFileByFileId")
	public ResponseVO updateVideoInfoFileByFileId(VideoInfoFile bean, String fileId) {
		this.videoInfoFileService.updateVideoInfoFileByFileId(bean, fileId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 FileId删除
	 */
	@RequestMapping("deleteVideoInfoFileByFileId")
	public ResponseVO deleteVideoInfoFileByFileId(String fileId) {
		this.videoInfoFileService.deleteVideoInfoFileByFileId(fileId);
		return getSuccessResponseVO(null);
	}

}