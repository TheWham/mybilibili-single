package com.easylive.service.impl;

import com.easylive.constants.Constants;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.query.VideoDanmuQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.VideoDanmuMapper;
import com.easylive.mappers.VideoInfoFileMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.VideoDanmuService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/09
 * 视频弹幕Service
 */

@Service("VideoDanmuService")
public class VideoDanmuServiceImpl implements VideoDanmuService {
	@Resource
	private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Autowired
    private VideoInfoFileMapper videoInfoFileMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<VideoDanmu> findListByParam(VideoDanmuQuery param) {
		return this.videoDanmuMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoDanmuQuery param) {
		return this.videoDanmuMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoDanmu> list = this.findListByParam(param);
		PaginationResultVO<VideoDanmu> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoDanmu bean) {
		return this.videoDanmuMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoDanmu>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoDanmuMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoDanmu> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoDanmuMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 DanmuId查询
	 */
	@Override
	public VideoDanmu getVideoDanmuByDanmuId(Integer danmuId) {
		return this.videoDanmuMapper.selectByDanmuId(danmuId);
	}

	/**
	 * 根据 DanmuId更新
	 */
	@Override
	public Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId) {
		return this.videoDanmuMapper.updateByDanmuId(bean, danmuId);
	}

	/**
	 * 根据 DanmuId删除
	 */
	@Override
	public Integer deleteVideoDanmuByDanmuId(Integer danmuId) {
		return this.videoDanmuMapper.deleteByDanmuId(danmuId);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void postDanmu(VideoDanmu videoDanmu) {
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoDanmu.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())){
			throw new BusinessException("up主已经关闭弹幕");
		}

		//借助原子更新解决弹幕发送高并发问题
		videoInfoMapper.updateCount(videoDanmu.getVideoId(), UserActionTypeEnum.VIDEO_DNAMU.getField(), 1);
		this.add(videoDanmu);
		//TODO 添加弹幕es
	}

	@Override
	public List<VideoDanmu> loadDanmu(String fileId, String videoId) {

		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);

		// 用户是否开通弹幕
		if (videoInfo == null || (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())))
			return List.of();

		VideoDanmuQuery danmuQuery = new VideoDanmuQuery();
		danmuQuery.setFileId(fileId);
		danmuQuery.setVideoId(videoId);
		return this.findListByParam(danmuQuery);

	}

}