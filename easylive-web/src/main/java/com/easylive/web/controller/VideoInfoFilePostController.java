package com.easylive.web.controller;

import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.VideoInfoFilePostService;
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
@RequestMapping("videoInfoFilePost")
public class VideoInfoFilePostController extends ABaseController {
	@Resource
	private VideoInfoFilePostService videoInfoFilePostService;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (VideoInfoFilePostQuery query) {
		return getSuccessResponseVO(videoInfoFilePostService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(VideoInfoFilePostQuery param) {
		return this.videoInfoFilePostService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<VideoInfoFilePost> findListByPage(VideoInfoFilePostQuery param) {
		Integer count = this.videoInfoFilePostService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoFilePost> list = this.videoInfoFilePostService.findListByParam(param);
		PaginationResultVO<VideoInfoFilePost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(VideoInfoFilePost bean) {
		videoInfoFilePostService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<VideoInfoFilePost> listBean) {
		videoInfoFilePostService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<VideoInfoFilePost> listBean) {
		videoInfoFilePostService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 FileId查询
	 */
	@RequestMapping("getVideoInfoFilePostByFileId")
	public ResponseVO getVideoInfoFilePostByFileId(String fileId) {
		return getSuccessResponseVO(this.videoInfoFilePostService.getVideoInfoFilePostByFileId(fileId));
	}

	/**
	 * @description 根据 FileId更新
	 */
	@RequestMapping("updateVideoInfoFilePostByFileId")
	public ResponseVO updateVideoInfoFilePostByFileId(VideoInfoFilePost bean, String fileId) {
		this.videoInfoFilePostService.updateVideoInfoFilePostByFileId(bean, fileId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 FileId删除
	 */
	@RequestMapping("deleteVideoInfoFilePostByFileId")
	public ResponseVO deleteVideoInfoFilePostByFileId(String fileId) {
		this.videoInfoFilePostService.deleteVideoInfoFilePostByFileId(fileId);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 UploadIdAndUserId查询
	 */
	@RequestMapping("getVideoInfoFilePostByUploadIdAndUserId")
	public ResponseVO getVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId) {
		return getSuccessResponseVO(this.videoInfoFilePostService.getVideoInfoFilePostByUploadIdAndUserId(uploadId, userId));
	}

	/**
	 * @description 根据 UploadIdAndUserId更新
	 */
	@RequestMapping("updateVideoInfoFilePostByUploadIdAndUserId")
	public ResponseVO updateVideoInfoFilePostByUploadIdAndUserId(VideoInfoFilePost bean, String uploadId, String userId) {
		this.videoInfoFilePostService.updateVideoInfoFilePostByUploadIdAndUserId(bean, uploadId, userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 UploadIdAndUserId删除
	 */
	@RequestMapping("deleteVideoInfoFilePostByUploadIdAndUserId")
	public ResponseVO deleteVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId) {
		this.videoInfoFilePostService.deleteVideoInfoFilePostByUploadIdAndUserId(uploadId, userId);
		return getSuccessResponseVO(null);
	}

}