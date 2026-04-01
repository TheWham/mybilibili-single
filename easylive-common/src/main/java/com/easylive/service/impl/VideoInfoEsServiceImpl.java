package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.easylive.config.AdminConfig;
import com.easylive.entity.dto.VideoInfoEsDTO;
import com.easylive.entity.po.VideoInfo;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoEsService;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

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
        try {
            if (isExistKey(adminConfig.getEsIndexVideoName(), videoInfo.getVideoId())){
                updateDoc(videoInfo);
            }else {
                VideoInfoEsDTO videoInfoEsDTO = BeanUtil.toBean(videoInfo, VideoInfoEsDTO.class);
                videoInfoEsDTO.setCollectCount(0);
                videoInfoEsDTO.setPlayCount(0);
                videoInfoEsDTO.setDanmuCount(0);

                elasticsearchClient.index(request-> request
                        .index(adminConfig.getEsIndexVideoName())
                        .id(videoInfoEsDTO.getVideoId())
                        .document(videoInfoEsDTO));
            }

        }catch (Exception e) {
            log.error("保存es数据库失败", e);
            throw new BusinessException("保存失败", e);
        }
    }

    @Override
    public void updateDoc(VideoInfo videoInfo) {
        try {
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            HashMap<String , Object> map = new HashMap<>();
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = videoInfo.getClass().getMethod(methodName);
                Object value = method.invoke(videoInfo);
                if (value != null && value instanceof String && !StringTools.isEmpty(value.toString()) || value != null && !(value instanceof String))
                {
                    map.put(field.getName(), value);
                }
            }
            if (map.isEmpty())
                return;

            elasticsearchClient.update(u -> u
                    .index(adminConfig.getEsIndexVideoName())
                    .id(videoInfo.getVideoId())
                    .doc(map)
                    .upsert(videoInfo),
                    VideoInfo.class
            );

        }catch (Exception e) {
            log.error("更新失败");
            throw new BusinessException("更新失败", e);
        }

    }

    private boolean isExistKey(String indexName, String id) throws IOException {

        return elasticsearchClient.get(g -> g
                        .index(indexName)
                        .id(id),
                VideoInfo.class
        ).found();
    }
}
