package com.easylive.service.impl;

import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.SeriesWithVideoVO;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserVideoSeriesMapper;
import com.easylive.mappers.UserVideoSeriesVideoMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserVideoSeriesService;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户视频列表Service
 */

@Service("UserVideoSeriesService")
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {
	@Resource
	private UserVideoSeriesMapper<UserVideoSeries, UserVideoSeriesQuery> userVideoSeriesMapper;
	@Resource
	private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> userVideoSeriesVideoMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param) {
		return this.userVideoSeriesMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserVideoSeriesQuery param) {
		return this.userVideoSeriesMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoSeries> list = this.findListByParam(param);
		PaginationResultVO<UserVideoSeries> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserVideoSeries bean) {
		return this.userVideoSeriesMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserVideoSeries>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoSeriesMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserVideoSeries> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoSeriesMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 SeriesId查询
	 */
	@Override
	public UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId) {
		return this.userVideoSeriesMapper.selectBySeriesId(seriesId);
	}

	/**
	 * 根据 SeriesId更新
	 */
	@Override
	public Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId) {
		return this.userVideoSeriesMapper.updateBySeriesId(bean, seriesId);
	}

	/**
	 * 根据 SeriesId删除
	 */
	@Override
	public Integer deleteUserVideoSeriesBySeriesIdAndUserId(Integer seriesId, String userId) {
		return this.userVideoSeriesMapper.deleteBySeriesIdAndUserId(seriesId, userId);
	}

	@Override
	public List<VideoInfo> selectAllVideoBySeriesIdAndUserId(Integer seriesId, String userId) {
		List<VideoInfo> videoInfoList = videoInfoMapper.selectVideoListBySeriesIdAndUserId(seriesId, userId);
		return videoInfoList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveVideoSeries(Integer seriesId, String seriesName, String seriesDescription, String videoIds, String userId) {
		if (seriesId == null && StringTools.isEmpty(videoIds))
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		String[] videoIdList = null;
		if (!StringTools.isEmpty(videoIds))
		{
			videoIdList = videoIds.split(",");
			checkVideoIdsValid(userId, videoIdList);
		}
		UserVideoSeries userVideoSeries = new UserVideoSeries();
		userVideoSeries.setSeriesDescription(seriesDescription);
		userVideoSeries.setSeriesName(seriesName);
		userVideoSeries.setUserId(userId);
		userVideoSeries.setSeriesId(seriesId);
		userVideoSeries.setUpdateTime(new Date());
		Integer maxSort = userVideoSeriesMapper.selectMaxSort(userId);
		userVideoSeries.setSort(maxSort + 1);
		if (seriesId == null)
		{
			//表示是新建分类列表
			userVideoSeriesMapper.insert(userVideoSeries);
			saveUserVideoSeriesVideo(userVideoSeries.getSeriesId(), videoIdList, userId);
			return;
		}
		UserVideoSeries oldVideoSeries = userVideoSeriesMapper.selectBySeriesId(seriesId);
		if (oldVideoSeries == null || !oldVideoSeries.getUserId().equals(userId))
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		userVideoSeriesMapper.insertOrUpdate(userVideoSeries);
	}

	@Override
	public void saveSeriesVideo(Integer seriesId, Integer sort, String videoIds, String userId) {
		if (videoIds == null || videoIds.isEmpty())
			return;

		if (sort == null)
		{
			Integer newSort = 0;
			//表示要操作两个视频的位置
			String [] vIds = videoIds.split(",");

			List<UserVideoSeriesVideo> updateVideoSeriesVideoList = new ArrayList<>(vIds.length);
			for (String videoId : vIds) {
				UserVideoSeriesVideo seriesVideo = new UserVideoSeriesVideo();
				seriesVideo.setSort(++newSort);
				seriesVideo.setVideoId(videoId);
				seriesVideo.setUserId(userId);
				seriesVideo.setSeriesId(seriesId);
				updateVideoSeriesVideoList.add(seriesVideo);
			}
			userVideoSeriesVideoMapper.insertOrUpdateBatch(updateVideoSeriesVideoList);
			return;
		}


		String [] vIds = videoIds.split(",");
		List<UserVideoSeriesVideo> addList = new ArrayList<>(vIds.length);
		for (String id : vIds)
		{
			UserVideoSeriesVideo userVideoSeriesVideo = new UserVideoSeriesVideo();
			userVideoSeriesVideo.setUserId(userId);
			userVideoSeriesVideo.setSeriesId(seriesId);
			userVideoSeriesVideo.setSort(++sort);
			userVideoSeriesVideo.setVideoId(id);
			addList.add(userVideoSeriesVideo);
		}
		userVideoSeriesVideoMapper.insertBatch(addList);
	}

	@Override
	public List<UserVideoSeries> loadVideoSeries(String userId) {
		return userVideoSeriesMapper.loadVideoSeries(userId);
	}

	@Override
	public void changeVideoSeriesSort(String videoSeriesIds, String userId) {
		String [] ids = videoSeriesIds.split(",");
		Integer[] intIds = Arrays.stream(ids).map(Integer::parseInt).toArray(Integer[]::new);
		if (ids.length == 0)
			return;
		Integer sort = 0;
		List<UserVideoSeries> updateList = new ArrayList<>(ids.length);
		for (Integer id : intIds)
		{
			UserVideoSeries userVideoSeries = new UserVideoSeries();
			userVideoSeries.setSeriesId(id);
			userVideoSeries.setSort(++sort);
			updateList.add(userVideoSeries);
		}
		userVideoSeriesMapper.updateSeriesSortBatch(updateList, userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer delVideoSeries(Integer seriesId, String userId) {
		//先删除该列表下的视频
		UserVideoSeriesVideoQuery seriesVideoQuery = new UserVideoSeriesVideoQuery();
		seriesVideoQuery.setSeriesId(seriesId);
		seriesVideoQuery.setUserId(userId);
		List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoMapper.selectList(seriesVideoQuery);

		if (seriesVideoList != null && !seriesVideoList.isEmpty())
		{
			List<String> seriesVideoIds = seriesVideoList.stream().map(UserVideoSeriesVideo::getVideoId).toList();
			userVideoSeriesVideoMapper.deleteByIds(seriesVideoIds, userId, seriesId);
		}

		return this.userVideoSeriesMapper.deleteBySeriesIdAndUserId(seriesId, userId);
	}

	@Override
	public List<SeriesWithVideoVO> selectVideoSeriesWithVideo(String userId) {
		List<SeriesWithVideoVO> seriesWithVideoVOS = userVideoSeriesMapper.selectVideoSeriesWithVideo(userId);
		return seriesWithVideoVOS;
	}


	private void checkVideoIdsValid(String userId, String[] videoIdList)
	{
		if (videoIdList == null || videoIdList.length == 0)
			return;
		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		videoInfoQuery.setUserId(userId);
		videoInfoQuery.setArrayIds(videoIdList);
		Integer count = videoInfoMapper.selectCount(videoInfoQuery);
		if (count != videoIdList.length)
			throw new BusinessException(ResponseCodeEnum.CODE_600);
	}

	private void saveUserVideoSeriesVideo(Integer seriesId, String[] videoIdList, String userId)
	{
		if (seriesId == null)
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		if (videoIdList == null || videoIdList.length == 0)
			throw new BusinessException("未发布视频");
		List<String> videoIdsInDB = userVideoSeriesVideoMapper.selectVideoIdsBySeriesIdAndUserId(seriesId, userId);
		List<String> newAddVideoIdList = List.of(videoIdList);
	   //用户可能选到已经添加过的视频
		List<String> newIds = newAddVideoIdList.stream().filter(id -> !videoIdsInDB.contains(id)).distinct().toList();

		if (newIds == null || newIds.isEmpty())
		{
			throw new BusinessException("所选视频已添加");
		}

		List<UserVideoSeriesVideo> userVideoSeriesVideoList = new ArrayList<>(newIds.size());
		Integer maxSort = userVideoSeriesVideoMapper.selectMaxSort(userId);
		for (String id : newIds) {
			UserVideoSeriesVideo userVideoSeriesVideo = new UserVideoSeriesVideo();
			userVideoSeriesVideo.setSeriesId(seriesId);
			userVideoSeriesVideo.setVideoId(id);
			userVideoSeriesVideo.setSort(++maxSort);
			userVideoSeriesVideo.setUserId(userId);
			userVideoSeriesVideoList.add(userVideoSeriesVideo);
		}
		userVideoSeriesVideoMapper.insertBatch(userVideoSeriesVideoList);
	}

}