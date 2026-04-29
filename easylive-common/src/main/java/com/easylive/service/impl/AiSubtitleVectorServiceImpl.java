package com.easylive.service.impl;

import com.easylive.config.AdminConfig;
import com.easylive.entity.vo.AiMatchedVideoVO;
import com.easylive.exception.BusinessException;
import com.easylive.service.AiSubtitleVectorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service("aiSubtitleVectorService")
public class AiSubtitleVectorServiceImpl implements AiSubtitleVectorService {

    private static final double ES_COSINE_SCORE_OFFSET = 1.0D;
    private static final List<String> SOURCE_FIELDS = Arrays.asList(
            "videoId", "videoName", "videoCover", "content", "startTime", "endTime"
    );

    @Resource
    private RestClient restClient;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AdminConfig adminConfig;

    @Override
    public List<AiMatchedVideoVO> search(List<Double> queryVector, Integer topK, Double minScore) {
        if (queryVector == null || queryVector.isEmpty()) {
            return new ArrayList<>();
        }
        int limit = topK == null || topK <= 0 ? adminConfig.getAiRagDefaultTopK() : topK;
        int candidateSize = Math.max(limit * 3, limit);

        try {
            Request request = new Request("POST", "/" + adminConfig.getEsIndexVideoSubtitleVectorName() + "/_search");
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(buildSearchBody(queryVector, candidateSize)), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return parseSearchResponse(responseJson, limit, minScore == null ? adminConfig.getAiRagMinScore() : minScore);
        } catch (ResponseException e) {
            if (e.getResponse() != null && e.getResponse().getStatusLine().getStatusCode() == 404) {
                log.warn("字幕向量索引不存在, index={}", adminConfig.getEsIndexVideoSubtitleVectorName());
                return new ArrayList<>();
            }
            log.error("字幕向量检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        } catch (Exception e) {
            log.error("字幕向量检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        }
    }

    @Override
    public void deleteByVideoId(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return;
        }
        try {
            Request request = new Request("POST", "/" + adminConfig.getEsIndexVideoSubtitleVectorName() + "/_delete_by_query");
            request.addParameter("conflicts", "proceed");
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(buildDeleteBody(videoId)), ContentType.APPLICATION_JSON));
            restClient.performRequest(request);
        } catch (ResponseException e) {
            if (e.getResponse() != null && e.getResponse().getStatusLine().getStatusCode() == 404) {
                return;
            }
            throw new BusinessException("删除视频字幕向量失败", e);
        } catch (Exception e) {
            throw new BusinessException("删除视频字幕向量失败", e);
        }
    }

    private Map<String, Object> buildSearchBody(List<Double> queryVector, int candidateSize) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("queryVector", queryVector);

        Map<String, Object> script = new LinkedHashMap<>();
        // ES 的 script_score 不接受负分，所以把 cosine 分数整体 +1，解析结果时再减回来。
        script.put("source", "cosineSimilarity(params.queryVector, 'contentVector') + 1.0");
        script.put("params", params);

        Map<String, Object> scriptScore = new LinkedHashMap<>();
        scriptScore.put("query", Map.of("match_all", Map.of()));
        scriptScore.put("script", script);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", candidateSize);
        body.put("_source", SOURCE_FIELDS);
        body.put("query", Map.of("script_score", scriptScore));
        return body;
    }

    private Map<String, Object> buildDeleteBody(String videoId) {
        return Map.of(
                "query", Map.of(
                        "term", Map.of(
                                "videoId", Map.of("value", videoId)
                        )
                )
        );
    }

    private List<AiMatchedVideoVO> parseSearchResponse(String responseJson, int limit, double minScore) throws Exception {
        JsonNode hits = objectMapper.readTree(responseJson).path("hits").path("hits");
        Map<String, AiMatchedVideoVO> videoMap = new LinkedHashMap<>();
        for (JsonNode hit : hits) {
            double cosineScore = hit.path("_score").asDouble(0D) - ES_COSINE_SCORE_OFFSET;
            if (cosineScore < minScore) {
                continue;
            }
            JsonNode source = hit.path("_source");
            String videoId = source.path("videoId").asText("");
            if (videoId.isEmpty() || videoMap.containsKey(videoId)) {
                continue;
            }

            AiMatchedVideoVO matchedVideo = new AiMatchedVideoVO();
            matchedVideo.setVideoId(videoId);
            matchedVideo.setVideoName(source.path("videoName").asText(""));
            matchedVideo.setVideoCover(source.path("videoCover").asText(""));
            matchedVideo.setMatchedText(source.path("content").asText(""));
            matchedVideo.setStartTime(source.path("startTime").asDouble(0D));
            matchedVideo.setEndTime(source.path("endTime").asDouble(0D));
            matchedVideo.setScore(roundScore(cosineScore));
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(videoMap.values()));
    }

    private double roundScore(double score) {
        return Math.round(score * 10000D) / 10000D;
    }
}
