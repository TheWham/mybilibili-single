package com.easylive.entity.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 前端 AI 问答请求。
 */
public class AiChatRequestDTO implements Serializable {

    /**
     * 用户本次想查询的问题或关键词。
     */
    @NotBlank(message = "关键词不能为空")
    private String keyword;

    /**
     * 最多返回几个相关视频，不传时使用后端默认值。
     */
    private Integer topK;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
