package com.easylive.web.controller;

import com.easylive.entity.po.VideoComment;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.VideoCommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * @author amani
 * @date 2026/03/09
 * @description 评论Service
 */

@RestController
@RequestMapping("videoComment")
public class VideoCommentController extends ABaseController {
	@Resource
	private VideoCommentService videoCommentService;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (VideoCommentQuery query) {
		return getSuccessResponseVO(videoCommentService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(VideoCommentQuery param) {
		return this.videoCommentService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
		Integer count = this.videoCommentService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoComment> list = this.videoCommentService.findListByParam(param);
		PaginationResultVO<VideoComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(VideoComment bean) {
		videoCommentService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<VideoComment> listBean) {
		videoCommentService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<VideoComment> listBean) {
		videoCommentService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 CommentId查询
	 */
	@RequestMapping("getVideoCommentByCommentId")
	public ResponseVO getVideoCommentByCommentId(Integer commentId) {
		return getSuccessResponseVO(this.videoCommentService.getVideoCommentByCommentId(commentId));
	}

	/**
	 * @description 根据 CommentId更新
	 */
	@RequestMapping("updateVideoCommentByCommentId")
	public ResponseVO updateVideoCommentByCommentId(VideoComment bean, Integer commentId) {
		this.videoCommentService.updateVideoCommentByCommentId(bean, commentId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 CommentId删除
	 */
	@RequestMapping("deleteVideoCommentByCommentId")
	public ResponseVO deleteVideoCommentByCommentId(Integer commentId) {
		this.videoCommentService.deleteVideoCommentByCommentId(commentId);
		return getSuccessResponseVO(null);
	}

}