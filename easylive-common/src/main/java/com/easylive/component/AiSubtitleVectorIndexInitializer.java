package com.easylive.component;

import com.easylive.config.AdminConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component("aiSubtitleVectorIndexInitializer")
public class AiSubtitleVectorIndexInitializer {

    private static final String AI_SUBTITLE_VECTOR_INDEX_TEMPLATE = "elasticsearch/video-subtitle-vector-index.json";

    @Resource
    private AdminConfig adminConfig;
    @Resource
    private ElasticSearchIndexHelper elasticSearchIndexHelper;

    public void create() {
        elasticSearchIndexHelper.createIndexIfAbsent(
                adminConfig.getEsIndexVideoSubtitleVectorName(),
                AI_SUBTITLE_VECTOR_INDEX_TEMPLATE
        );
    }
}
