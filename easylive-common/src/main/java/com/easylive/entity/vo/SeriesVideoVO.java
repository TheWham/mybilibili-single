package com.easylive.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author amani
 */
@Data
public class SeriesVideoVO {
    private String videoId;
    private String videoName;
    private String videoCover;
    private Date createTime;
    private Integer playCount;
}
