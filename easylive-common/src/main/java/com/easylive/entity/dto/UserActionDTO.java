package com.easylive.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserActionDTO {
    /**
     * 视频id
     */
    @NotEmpty
    private String videoId;
    /**
     * 用户操作类型
     */
    @NotNull
    private Integer actionType;

    /**
     * 操作数量
     */
    private Integer actionCount;

}
