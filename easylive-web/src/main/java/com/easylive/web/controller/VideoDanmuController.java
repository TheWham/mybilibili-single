package com.easylive.web.controller;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.annotaion.GlobalInterceptor;
import com.easylive.entity.dto.VideoDanmuDTO;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author amani
 * @date 2026/03/09
 * @description 视频弹幕Service
 */

@RestController
@RequestMapping("danmu")
public class VideoDanmuController extends ABaseController {
	@Resource
	private VideoDanmuService videoDanmuService;

	@RequestMapping("/postDanmu")
	@GlobalInterceptor(checkLogin = true)
	public ResponseVO postDanmu(@Validated VideoDanmuDTO videoDanmuDTO)
	{
		VideoDanmu videoDanmu = BeanUtil.toBean(videoDanmuDTO, VideoDanmu.class);
		videoDanmu.setUserId(getTokenUserInfo().getUserId());
		videoDanmu.setPostTime(new Date());
		videoDanmuService.postDanmu(videoDanmu);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/loadDanmu")
	public ResponseVO loadDanmu(@NotEmpty String fileId, @NotEmpty String videoId)
	{
		List<VideoDanmu> danmuList = videoDanmuService.loadDanmu(fileId, videoId);
		return getSuccessResponseVO(danmuList);
	}


}