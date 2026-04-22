package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.config.AdminConfig;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.SysSettingDTO;
import com.easylive.entity.dto.VideoCountDTO;
import com.easylive.entity.dto.VideoCountUpdateDTO;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.VideoInfoFileMapper;
import com.easylive.mappers.VideoInfoFilePostMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.mappers.VideoInfoPostMapper;
import com.easylive.service.VideoEsService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息Service
 */

@Service("VideoInfoService")
public class VideoInfoServiceImpl implements VideoInfoService {
	private static final Logger log = LoggerFactory.getLogger(VideoInfoServiceImpl.class);
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;
	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

	@Resource
	private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;
	@Resource
	private RedisComponent redisComponent;
	@Resource
	private AdminConfig adminConfig;
	@Resource
	private VideoEsService videoEsService;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoInfo> findListByParam(VideoInfoQuery param) {
		List<VideoInfo> list = this.videoInfoMapper.selectList(param);
		mergeRedisActionDelta(list);
		return list;
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoInfoQuery param) {
		return this.videoInfoMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfo> list = this.findListByParam(param);
		PaginationResultVO<VideoInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoInfo bean) {
		return this.videoInfoMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfo>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * @description 单例新增/修改
	 */
	@Override
	public Integer addOrUpdate(VideoInfo videoBean) {
		if (videoBean == null) {
			return 0;
		}
		return this.videoInfoMapper.insertOrUpdate(videoBean);
	}

	@Override
	public void auditVideo(String videoId, Integer status, String reason) {
		//校验参数
		VideoInfoPost videoInfoPost = this.videoInfoPostMapper.selectByVideoId(videoId);
		if (videoInfoPost == null ||(!status.equals(VideoStatusEnum.STATUS_3.getStatus()) && !status.equals(VideoStatusEnum.STATUS_4.getStatus()))){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		VideoInfoPostQuery postQuery = new VideoInfoPostQuery();
		postQuery.setVideoId(videoId);
		postQuery.setStatus(VideoStatusEnum.STATUS_2.getStatus());
		VideoInfoPost updateInfoPost = new VideoInfoPost();
		updateInfoPost.setStatus(status);

		//update info
		Integer updateCount = videoInfoPostMapper.updateByCondition(updateInfoPost, postQuery);
		if (updateCount == 0)
			throw new BusinessException("正式表更新失败");

		if (status.equals(VideoStatusEnum.STATUS_4.getStatus()))
		{
			return;
		}

		//审核完成修改更新方式为未更新
		VideoInfoFilePost videoInfoFilePost = new VideoInfoFilePost();
		videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.UN_UPDATE.getStatus());
		VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
		filePostQuery.setVideoId(videoId);
		videoInfoFilePostMapper.updateByCondition(videoInfoFilePost, filePostQuery);

		//同步信息到正式表
		VideoInfo videoInfo = this.getVideoInfoByVideoId(videoId);
		boolean firstAuditPass = videoInfo == null;

		videoInfo = BeanUtil.toBean(videoInfoPost, VideoInfo.class);
		this.addOrUpdate(videoInfo);

		//不是第一次,表示有信息新增或者修改, 需要先清空再添加
		VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
		videoInfoFileQuery.setVideoId(videoId);
		videoInfoFileMapper.deleteByCondition(videoInfoFileQuery);

		//更新videoInfoFile表信息
		List<VideoInfoFilePost> filePostList = videoInfoFilePostMapper.selectList(filePostQuery);
		List<VideoInfoFile> videoInfoFiles = BeanUtil.copyToList(filePostList, VideoInfoFile.class);
		videoInfoFileMapper.insertOrUpdateBatch(videoInfoFiles);


		if (firstAuditPass) {
			SysSettingDTO sysSetting = redisComponent.getSysSetting();
			Integer rewardCoinCount = sysSetting.getPostVideoCoinCount();

			// 首次审核通过才发发布奖励。
			// 这里不直接改 MySQL，而是先补 Redis 实时值，再丢到异步队列里统一刷库，
			// 这样和你项目里其他互动计数的落库方式保持一致，前台也能马上读到新硬币数。
			redisComponent.addVideoAuditReward(videoInfoPost.getUserId(), videoId, rewardCoinCount);
		}

		//清楚更新时候被删除的文件
		List<String> deleteFilePathList = redisComponent.getDelFilePathsQueue(videoId);
		if (deleteFilePathList != null && !deleteFilePathList.isEmpty()) {
			deleteFilePathList.forEach(filePath -> {
				String completeFilePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + filePath;
				File file = new File(completeFilePath);
				if (file.exists()) {
					try {
						FileUtils.deleteDirectory(file);
					} catch (IOException e) {
						log.error("删除文件失败");
					}
				}
			});
		}


		//清空缓存
		redisComponent.cleanDelFilePaths(videoId);
		//保存到es
		videoEsService.saveDoc(videoInfo);
	}

	@Override
	public Integer reportVideoPlayOnline(String fileId, String deviceId) {
		Integer count = redisComponent.reportVideoPlayOnline(fileId, deviceId);
		return count;
	}

	@Override
	public VideoCountDTO sumVideoCountByUserId(String userId) {
		return this.videoInfoMapper.sumVideoCountByUserId(userId);
	}

	@Override
	public List<VideoInfo> selectByIds(List<String> userCollectionIds) {
		List<VideoInfo> videoInfoList = videoInfoMapper.selectByIds(userCollectionIds);
		mergeRedisActionDelta(videoInfoList);
		return videoInfoList;
	}

	@Override
	public List<VideoInfo> selectVideoListBySeriesIdAndUserId(Integer seriesId, String userId) {
		List<VideoInfo> videoInfoList = videoInfoMapper.selectVideoListBySeriesIdAndUserId(seriesId, userId);
		mergeRedisActionDelta(videoInfoList);
		return videoInfoList;
	}

	@Override
	public Integer updateCountBatch(String field, List<VideoCountUpdateDTO> list) {
		Integer count = this.videoInfoMapper.updateCountBatch(field, list);
		return count;
	}

	@Override
	public void recommendVideo(String videoId) {
		VideoInfo videoInfo = this.videoInfoMapper.selectByVideoId(videoId);
		VideoInfoPost videoInfoPost = this.videoInfoPostMapper.selectByVideoId(videoId);

		Optional.ofNullable(videoInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

		if (!(videoInfoPost.getStatus().equals(VideoStatusEnum.STATUS_3.getStatus())))
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		VideoInfo updateInfo = new VideoInfo();

		if (videoInfo.getRecommendType().equals(VideoRecommendEnum.NO_RECOMMEND.getStatus()))
			updateInfo.setRecommendType(VideoRecommendEnum.RECOMMEND.getStatus());

		if (videoInfo.getRecommendType().equals(VideoRecommendEnum.RECOMMEND.getStatus()))
			updateInfo.setRecommendType(VideoRecommendEnum.NO_RECOMMEND.getStatus());

		this.videoInfoMapper.updateByVideoId(updateInfo, videoId);
	}

	/**
	 * @description 根据 VideoId查询
	 */
	@Override
	public VideoInfo getVideoInfoByVideoId(String videoId) {
		VideoInfo videoInfo = this.videoInfoMapper.selectByVideoId(videoId);
		if (videoInfo == null) {
			return null;
		}
		log.info(
				"getVideoInfoByVideoId db snapshot, videoId={}, likeCount={}, collectCount={}, coinCount={}",
				videoId,
				defaultValue(videoInfo.getLikeCount()),
				defaultValue(videoInfo.getCollectCount()),
				defaultValue(videoInfo.getCoinCount())
		);
		mergeRedisActionDelta(videoInfo);
		log.info(
				"getVideoInfoByVideoId merged snapshot, videoId={}, likeCount={}, collectCount={}, coinCount={}",
				videoId,
				defaultValue(videoInfo.getLikeCount()),
				defaultValue(videoInfo.getCollectCount()),
				defaultValue(videoInfo.getCoinCount())
		);
		return videoInfo;
	}

	/**
	 * @description 根据 VideoId更新
	 */
	@Override
	public Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
		return this.videoInfoMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * @description 根据 VideoId删除
	 */
	@Override
	public Integer deleteVideoInfoByVideoId(String videoId) {
		return this.videoInfoMapper.deleteByVideoId(videoId);
	}

	@Override
	public Integer updateByCondition(VideoInfo videoInfo, VideoInfoQuery videoInfoQuery) {
		return this.videoInfoMapper.updateByCondition(videoInfo, videoInfoQuery);
	}

	private void mergeRedisActionDelta(VideoInfo videoInfo) {
		if (videoInfo == null) {
			return;
		}
		Map<String, Integer> deltaMap = redisComponent.getVideoActionCountDelta(videoInfo.getVideoId());
		if (deltaMap == null || deltaMap.isEmpty()) {
			log.info("getVideoInfoByVideoId redis delta empty, videoId={}", videoInfo.getVideoId());
			return;
		}
		log.info(
				"getVideoInfoByVideoId redis delta, videoId={}, likeDelta={}, collectDelta={}, coinDelta={}",
				videoInfo.getVideoId(),
				deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_LIKE.getField(), 0),
				deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COLLECT.getField(), 0),
				deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COIN.getField(), 0)
		);

		// 视频详情页要求用户操作后刷新立刻能看到结果。
		// MySQL 里的计数是异步同步的，所以这里把 Redis 中尚未落库的增量补到返回值上。
		videoInfo.setLikeCount(nonNegative(defaultValue(videoInfo.getLikeCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_LIKE.getField(), 0)));
		videoInfo.setCollectCount(nonNegative(defaultValue(videoInfo.getCollectCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COLLECT.getField(), 0)));
		videoInfo.setCoinCount(nonNegative(defaultValue(videoInfo.getCoinCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COIN.getField(), 0)));
	}

	private void mergeRedisActionDelta(List<VideoInfo> videoInfoList) {
		if (videoInfoList == null || videoInfoList.isEmpty()) {
			return;
		}
		// 前台列表、推荐列表和收藏列表之前都是直接查 MySQL。
		// 详情页已经补了 Redis 增量，如果列表页不补，就会出现“详情页是新值，列表还是旧值”的割裂感。
		for (VideoInfo videoInfo : videoInfoList) {
			mergeRedisActionDelta(videoInfo);
		}
	}

	private int defaultValue(Integer value) {
		return value == null ? 0 : value;
	}

	private int nonNegative(int value) {
		return Math.max(value, 0);
	}
}
