package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.VideoInfoPostDTO;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.VideoInfoPostMapper;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.service.VideoInfoPostService;
import com.easylive.utils.JsonUtils;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author amani
 * @date 2026/02/13
 * @description 视频信息Service
 */

@Service("VideoInfoPostService")
public class VideoInfoPostServiceImpl implements VideoInfoPostService {
	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private VideoInfoFilePostService videoInfoFilePostService;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoInfoPost> findListByParam(VideoInfoPostQuery param) {
		return this.videoInfoPostMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoInfoPostQuery param) {
		return this.videoInfoPostMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoPost> list = this.findListByParam(param);
		PaginationResultVO<VideoInfoPost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoInfoPost bean) {
		return this.videoInfoPostMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfoPost>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoPostMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfoPost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoPostMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 VideoId查询
	 */
	@Override
	public VideoInfoPost getVideoInfoPostByVideoId(String videoId) {
		return this.videoInfoPostMapper.selectByVideoId(videoId);
	}

	/**
	 * @description 根据 VideoId更新
	 */
	@Override
	public Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId) {
		return this.videoInfoPostMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * @description 根据 VideoId删除
	 */
	@Override
	public Integer deleteVideoInfoPostByVideoId(String videoId) {
		return this.videoInfoPostMapper.deleteByVideoId(videoId);
	}

	@Override
	public void savePostVideoInfo(VideoInfoPostDTO videoInfoPostDTO) {
		VideoInfoPost videoInfoPost = BeanUtil.toBean(videoInfoPostDTO, VideoInfoPost.class);
		String userId = videoInfoPost.getUserId();
		String videoID = videoInfoPost.getVideoId();
		String uploadFileList = videoInfoPostDTO.getUploadFileList();
		List<VideoInfoFilePost> uploadFilesInfo = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);

		if (uploadFilesInfo == null || uploadFilesInfo.size() > redisComponent.getSysSetting().getVideoCount())
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		//删除列表
		List<VideoInfoFilePost> deleteList = new ArrayList<>();
		//新增列表
		List<VideoInfoFilePost> addList = uploadFilesInfo;
		// 根据前端是否传过来videoId来判断是添加还是修改操作
		if (!StringTools.isEmpty(videoInfoPostDTO.getVideoId()))
		{
			//修改操作
			VideoInfoPost videoInfoPostDb = videoInfoPostMapper.selectByVideoId(videoInfoPostDTO.getVideoId());
			//判断修改的视频是否存在
			if (videoInfoPostDb == null)
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			//判断修改视频状态是否符合要求
			if (ArrayUtils.contains(new Integer[]{VideoStatusEnum.STATUS_0.getStatus(), VideoStatusEnum.STATUS_2.getStatus()}, videoInfoPostDb.getStatus()))
				throw new BusinessException(ResponseCodeEnum.CODE_600);

			VideoInfoFilePostQuery query = new VideoInfoFilePostQuery();
			query.setVideoId(videoID);
			query.setUserId(videoInfoPostDTO.getUserId());

			//在数据库中找到已存到数据库中视频列表
			List<VideoInfoFilePost> filesInfoInPostDb = videoInfoFilePostService.findListByParam(query);

			//将修改后文件列表
			Map<String, VideoInfoFilePost> videoInfoFileReflect = uploadFilesInfo.stream()
					.collect(Collectors.toMap(item -> item.getUploadId(), Function.identity(), (ans1, ans2) -> ans2));

			Boolean isUpdateFileName = false;
			for (VideoInfoFilePost filePost : filesInfoInPostDb)
			{
				VideoInfoFilePost isInDb = videoInfoFileReflect.get(filePost.getUploadId());
				//在修改视频文件列表中找出被删除了视频文件
				if (isInDb == null)
					deleteList.add(isInDb);
				//判断是否有文件名改变
				if (!isInDb.getFileName().equals(filePost.getFileName()))
					isUpdateFileName = true;
			}
			//判断是否改变了文件信息
			Boolean changeVideoInfoPost = isChangeVideoInfoPost(videoInfoPostDTO);

			//通过新提交的文件中找到没有fileId的新增文件
			addList = uploadFilesInfo.stream().filter(fileInfo -> fileInfo.getFileId() == null).toList();

			if (addList != null && addList.isEmpty())
			{
				//改为修改状态
				if (isUpdateFileName || changeVideoInfoPost)
					videoInfoPost.setStatus(VideoStatusEnum.STATUS_2.getStatus());
			}else{
				//新增改为默认状态
				videoInfoPost.setStatus(VideoStatusEnum.STATUS_0.getStatus());
			}
			videoInfoPostMapper.updateByVideoId(videoInfoPost, videoID);

		}else{
			//新增操作
			videoID = StringTools.generateRandomStr(Constants.LENGTH_10);
			//操作videoInfoPost文件
			videoInfoPost.setCreateTime(new Date());
			videoInfoPost.setLastUpdateTime(new Date());
			videoInfoPost.setStatus(VideoStatusEnum.STATUS_0.getStatus());
			videoInfoPost.setVideoId(videoID);
			videoInfoPostMapper.insert(videoInfoPost);
		}

		//操作videoInfoFilePost文件
		Integer index = 1;
		for (VideoInfoFilePost file: uploadFilesInfo)
		{
			file.setFileIndex(index++);
			file.setUserId(userId);
			file.setVideoId(videoID);
			if (file.getFileId() == null)
			{
				file.setFileId(StringTools.generateRandomStr(Constants.LENGTH_20));
				file.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
				file.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
			}
		}

		this.videoInfoFilePostService.addOrUpdateBatch(uploadFilesInfo);
		if (deleteList != null && !deleteList.isEmpty())
		{
			List<String> deleteListIds = deleteList.stream().map(VideoInfoFilePost::getFileId).collect(Collectors.toList());
			videoInfoPostMapper.delBatchByIds(deleteListIds, userId);
			List<String> filePathList = deleteList.stream().map(VideoInfoFilePost::getFilePath).toList();
			redisComponent.addFileList2DelQueue(videoID, filePathList);
		}

		if (addList != null && !addList.isEmpty())
		{
			for (VideoInfoFilePost addFile : addList)
			{
				addFile.setVideoId(videoID);
				addFile.setUserId(userId);
			}
			redisComponent.addFileList2TransferQueue(addList);
		}

	}
	private Boolean isChangeVideoInfoPost(VideoInfoPostDTO videoInfoPost)
	{
		VideoInfoPost currentVideoInfo = videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
		// 简介, 标签, 标题, 封面
		return !currentVideoInfo.getTags().equals(videoInfoPost.getTags())
				|| !currentVideoInfo.getIntroduction().equals(videoInfoPost.getIntroduction())
				|| !currentVideoInfo.getVideoCover().equals(videoInfoPost.getVideoCover())
				|| !currentVideoInfo.getVideoName().equals(videoInfoPost.getVideoName());
	}

}