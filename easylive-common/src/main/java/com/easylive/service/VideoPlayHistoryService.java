package com.easylive.service;

import com.easylive.entity.po.VideoPlayHistory;
import com.easylive.entity.query.VideoPlayHistoryQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史Service
 */
public interface VideoPlayHistoryService {

	/**
	 * 根据条件查询
	 */
	List<VideoPlayHistory> findListByParam(VideoPlayHistoryQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(VideoPlayHistoryQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery param);

	/**
	 * 新增
	 */
	Integer add(VideoPlayHistory bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoPlayHistory>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoPlayHistory> listBean);


	/**
	 * 根据 UserIdAndVideoId查询
	 */
	VideoPlayHistory getVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);

	/**
	 * 根据 UserIdAndVideoId更新
	 */
	Integer updateVideoPlayHistoryByUserIdAndVideoId(VideoPlayHistory bean, String userId, String videoId);

	/**
	 * 根据 UserIdAndVideoId删除
	 */
	Integer deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);

}