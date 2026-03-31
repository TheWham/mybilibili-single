package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.easylive.config.AdminConfig;
import com.easylive.entity.dto.VideoInfoEsDTO;
import com.easylive.entity.po.VideoInfo;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author amani
 */

@Slf4j
@Service("videoEsService")
public class VideoInfoEsServiceImpl implements VideoEsService {

    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private AdminConfig adminConfig;

    @Override
    public void saveDoc(VideoInfo videoInfo) {
        VideoInfoEsDTO videoInfoEsDTO = BeanUtil.toBean(videoInfo, VideoInfoEsDTO.class);
        videoInfoEsDTO.setCollectCount(0);
        videoInfoEsDTO.setPlayCount(0);
        videoInfoEsDTO.setDanmuCount(0);
        try {
            elasticsearchClient.index(request-> request
                    .index(adminConfig.getEsIndexVideoName())
                    .id(videoInfoEsDTO.getVideoId())
                    .document(videoInfoEsDTO));
        } catch (IOException e) {
            log.error("保存到es失败");
            throw new BusinessException("保存失败", e);
        }
    }
}
