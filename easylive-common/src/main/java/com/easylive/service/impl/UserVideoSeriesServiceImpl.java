package com.easylive.service.impl;

import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
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
	public Integer deleteUserVideoSeriesBySeriesId(Integer seriesId) {
		return this.userVideoSeriesMapper.deleteBySeriesId(seriesId);
	}

	@Override
	public List<VideoInfo> selectAllVideoBySeriesId(Integer seriesId, String userId) {
		List<VideoInfo> videoInfoList = videoInfoMapper.selectVideoListBySeriesIdAndUserId(seriesId, userId);
		return videoInfoList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUserVideoSeries(Integer seriesId, String seriesName, String seriesDescription, String videoIds, String userId) {
		if (seriesId == null && StringTools.isEmpty(videoIds))
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		String[] videoIdList = videoIds.split(",");
		checkVideoIdsValid(userId, videoIdList);
		UserVideoSeries userVideoSeries = new UserVideoSeries();
		userVideoSeries.setSeriesDescription(seriesDescription);
		userVideoSeries.setSeriesName(seriesName);
		userVideoSeries.setUserId(userId);
		userVideoSeries.setSeriesId(seriesId);
		userVideoSeries.setUpdateTime(new Date());
		if (seriesId == null)
		{
			//表示是新建分类列表
			Integer maxSort = userVideoSeriesMapper.selectMaxSort(userId);
			userVideoSeries.setSort(maxSort + 1);
			userVideoSeriesMapper.insert(userVideoSeries);
			saveUserVideoSeriesVideo(userVideoSeries.getSeriesId(), videoIdList, userId);
		}
		UserVideoSeries oldVideoSeries = userVideoSeriesMapper.selectBySeriesId(seriesId);
		if (oldVideoSeries == null || !oldVideoSeries.getUserId().equals(userId))
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		userVideoSeriesMapper.insertOrUpdate(userVideoSeries);
	}

	private void checkVideoIdsValid(String userId, String[] videoIdList)
	{
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

		List<UserVideoSeriesVideo> userVideoSeriesVideoList = new ArrayList<>(videoIdList.length);
		Integer maxSort = userVideoSeriesVideoMapper.selectMaxSort(userId);
		for (String id : videoIdList) {
			UserVideoSeriesVideo userVideoSeriesVideo = new UserVideoSeriesVideo();
			userVideoSeriesVideo.setSeriesId(seriesId);
			userVideoSeriesVideo.setVideoId(id);
			userVideoSeriesVideo.setSort(++maxSort);
			userVideoSeriesVideoList.add(userVideoSeriesVideo);
		}
		userVideoSeriesVideoMapper.insertBatch(userVideoSeriesVideoList);
	}

}