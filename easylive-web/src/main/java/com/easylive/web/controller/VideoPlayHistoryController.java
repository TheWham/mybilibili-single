package com.easylive.web.controller;

import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.VideoPlayHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史Service
 */

@RestController
@RequestMapping("history")
public class VideoPlayHistoryController extends ABaseController {
	@Resource
	private VideoPlayHistoryService videoPlayHistoryService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadHistory")
	public ResponseVO loadDataList (Integer pageNo) {
		return getSuccessResponseVO(videoPlayHistoryService.loadHistoryByPage(getTokenUserInfo().getUserId(), pageNo));
	}

}
