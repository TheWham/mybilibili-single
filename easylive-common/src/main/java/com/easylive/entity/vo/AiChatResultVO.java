package com.easylive.entity.vo;

import java.io.Serializable;
import java.util.List;

/**
 * AI 问答返回结果。
 */
public class AiChatResultVO implements Serializable {

    private String answer;
    private List<AiMatchedVideoVO> videos;
    private List<String> suggestions;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<AiMatchedVideoVO> getVideos() {
        return videos;
    }

    public void setVideos(List<AiMatchedVideoVO> videos) {
        this.videos = videos;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
