package com.easylive.web.service.impl;

import com.easylive.config.AdminConfig;
import com.easylive.entity.dto.AiChatRequestDTO;
import com.easylive.entity.vo.AiChatResultVO;
import com.easylive.entity.vo.AiMatchedVideoVO;
import com.easylive.exception.BusinessException;
import com.easylive.service.AiSubtitleVectorService;
import com.easylive.web.service.AiChatService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("aiChatService")
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);
    private static final int EMBEDDING_DIMENSION = 1024;

    @Resource
    private ChatClient.Builder chatClientBuilder;
    @Resource
    private EmbeddingModel embeddingModel;
    @Resource
    private AiSubtitleVectorService aiSubtitleVectorService;
    @Resource
    private AdminConfig adminConfig;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AiChatResultVO chat(AiChatRequestDTO request) {
        String keyword = StringUtils.trimToEmpty(request.getKeyword());
        int topK = normalizeTopK(request.getTopK());

        List<Double> queryVector = buildQueryVector(keyword);
        List<AiMatchedVideoVO> matchedVideos = aiSubtitleVectorService.search(queryVector, topK, adminConfig.getAiRagMinScore());

        AiChatResultVO resultVO = new AiChatResultVO();
        resultVO.setVideos(matchedVideos);
        if (matchedVideos == null || matchedVideos.isEmpty()) {
            resultVO.setAnswer(buildNoMatchAnswer(keyword));
            resultVO.setSuggestions(buildNoMatchSuggestions(keyword));
            return resultVO;
        }

        resultVO.setAnswer(buildAiAnswer(keyword, matchedVideos));
        resultVO.setSuggestions(buildFollowUpSuggestions(keyword, matchedVideos));
        return resultVO;
    }

    private List<Double> buildQueryVector(String keyword) {
        try {
            float[] output = embeddingModel.embed(keyword);
            if (output == null || output.length == 0) {
                throw new BusinessException("AI 向量模型没有返回结果");
            }
            if (output.length != EMBEDDING_DIMENSION) {
                log.warn("查询向量维度和字幕索引不一致, expected={}, actual={}", EMBEDDING_DIMENSION, output.length);
            }
            List<Double> vector = new ArrayList<>(output.length);
            for (float value : output) {
                vector.add((double) value);
            }
            return vector;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成查询向量失败, keyword={}", keyword, e);
            throw new BusinessException("AI 向量模型暂时不可用，请稍后再试", e);
        }
    }

    private String buildAiAnswer(String keyword, List<AiMatchedVideoVO> matchedVideos) {
        String prompt = buildPrompt(keyword, matchedVideos);
        try {
            String answer = chatClient.prompt()
                    .system("你是 Easylive 的视频内容助手。回答要基于给定视频字幕片段，不要编造片段里没有的信息。")
                    .user(prompt)
                    .call()
                    .content();
            if (StringUtils.isBlank(answer)) {
                throw new BusinessException("AI 模型没有返回回答");
            }
            return answer.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 问答模型调用失败, keyword={}", keyword, e);
            throw new BusinessException("AI 问答模型暂时不可用，请稍后再试", e);
        }
    }

    private String buildPrompt(String keyword, List<AiMatchedVideoVO> matchedVideos) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < matchedVideos.size(); i++) {
            AiMatchedVideoVO video = matchedVideos.get(i);
            context.append("【视频").append(i + 1).append("】")
                    .append(video.getVideoName()).append("\n")
                    .append("命中字幕：").append(video.getMatchedText()).append("\n")
                    .append("时间：").append(formatTime(video.getStartTime())).append(" - ")
                    .append(formatTime(video.getEndTime())).append("\n\n");
        }
        return "用户问题：" + keyword + "\n\n"
                + "可参考的视频字幕片段：\n" + context + "\n"
                + "请用中文给出一段自然、简洁的回答，先总结能查到的内容，再指出可以观看哪些相关视频。";
    }

    private String buildNoMatchAnswer(String keyword) {
        return "暂时没有找到和“" + keyword + "”高度匹配的视频内容。你可以换一个更具体的关键词，"
                + "比如技术名词、业务场景、功能名称，或者先查询相关视频列表再继续提问。";
    }

    private List<String> buildNoMatchSuggestions(String keyword) {
        List<String> suggestions = new ArrayList<>(3);
        suggestions.add("换一个更具体的关键词重新查询");
        suggestions.add("查询和“" + keyword + "”相关的视频");
        suggestions.add("按分类或热门视频先筛选内容");
        return suggestions;
    }

    private List<String> buildFollowUpSuggestions(String keyword, List<AiMatchedVideoVO> matchedVideos) {
        if (matchedVideos == null || matchedVideos.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> suggestions = new ArrayList<>(3);
        suggestions.add("继续了解“" + keyword + "”的实现细节");
        suggestions.add("查看《" + matchedVideos.get(0).getVideoName() + "》里的相关片段");
        suggestions.add("查询这个主题下更多相似视频");
        return suggestions;
    }

    private int normalizeTopK(Integer topK) {
        int defaultTopK = adminConfig.getAiRagDefaultTopK() == null ? 5 : adminConfig.getAiRagDefaultTopK();
        int maxTopK = adminConfig.getAiRagMaxTopK() == null ? 10 : adminConfig.getAiRagMaxTopK();
        if (topK == null || topK <= 0) {
            return defaultTopK;
        }
        return Math.min(topK, maxTopK);
    }

    private String formatTime(Double seconds) {
        if (seconds == null) {
            return "0.0s";
        }
        return seconds + "s";
    }
}
