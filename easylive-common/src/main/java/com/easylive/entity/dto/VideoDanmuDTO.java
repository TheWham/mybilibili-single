package com.easylive.entity.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoDanmuDTO {

    /**
     * 弹幕的具体文本内容，限制最大长度为300个字符
     */
    @NotEmpty
    @Size(max = 300)
    private String text;

    /**
     * 视频文件id
     */
    @NotEmpty
    private String fileId;

    /**
     * 弹幕出现的时间点，以秒为单位，不能为null
     */
    @NotNull
    private Integer time;
    /**
     * 关联的视频ID，标识弹幕所属的视频，不能为空
     */
    @NotEmpty
    private String videoId;
    /**
     * 弹幕的显示模式，如普通弹幕、高级弹幕等，可为空
     */
    private Integer mode;
    /**
     * 弹幕的颜色，默认可能为系统预设颜色，可为空
     */
    private String color;


}
