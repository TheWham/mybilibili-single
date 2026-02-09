package com.easylive.service;

import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.query.VideoInfoFileQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息Service
 */
public interface VideoInfoFileService {

	/**
	 * @description 根据条件查询
	 */
	List<VideoInfoFile> findListByParam(VideoInfoFileQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoFileQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<VideoInfoFile> findListByPage(VideoInfoFileQuery param);

	/**
	 * @description 新增
	 */
	Integer add(VideoInfoFile bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<VideoInfoFile>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoInfoFile> listBean);


	/**
	 * @description 根据 FileId查询
	 */
	VideoInfoFile getVideoInfoFileByFileId(String fileId);

	/**
	 * @description 根据 FileId更新
	 */
	Integer updateVideoInfoFileByFileId(VideoInfoFile bean, String fileId);

	/**
	 * @description 根据 FileId删除
	 */
	Integer deleteVideoInfoFileByFileId(String fileId);

}