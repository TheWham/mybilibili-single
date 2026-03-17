package com.easylive.service.impl;

import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.mappers.UserVideoSeriesVideoMapper;
import com.easylive.service.UserVideoSeriesVideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频Service
 */

@Service("UserVideoSeriesVideoService")
public class UserVideoSeriesVideoServiceImpl implements UserVideoSeriesVideoService {
	@Resource
	private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> userVideoSeriesVideoMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserVideoSeriesVideo> findListByParam(UserVideoSeriesVideoQuery param) {
		return this.userVideoSeriesVideoMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserVideoSeriesVideoQuery param) {
		return this.userVideoSeriesVideoMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserVideoSeriesVideo> findListByPage(UserVideoSeriesVideoQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoSeriesVideo> list = this.findListByParam(param);
		PaginationResultVO<UserVideoSeriesVideo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserVideoSeriesVideo bean) {
		return this.userVideoSeriesVideoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserVideoSeriesVideo>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoSeriesVideoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserVideoSeriesVideo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoSeriesVideoMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 SeriesIdAndVideoId查询
	 */
	@Override
	public UserVideoSeriesVideo getUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId) {
		return this.userVideoSeriesVideoMapper.selectBySeriesIdAndVideoId(seriesId, videoId);
	}

	/**
	 * 根据 SeriesIdAndVideoId更新
	 */
	@Override
	public Integer updateUserVideoSeriesVideoBySeriesIdAndVideoId(UserVideoSeriesVideo bean, Integer seriesId, String videoId) {
		return this.userVideoSeriesVideoMapper.updateBySeriesIdAndVideoId(bean, seriesId, videoId);
	}

	/**
	 * 根据 SeriesIdAndVideoId删除
	 */
	@Override
	public Integer deleteUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId) {
		return this.userVideoSeriesVideoMapper.deleteBySeriesIdAndVideoId(seriesId, videoId);
	}

}