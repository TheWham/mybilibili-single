package com.easylive.entity.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    // 用户ID
    private String userId;
    // 用户昵称
    private String nickName;
    // 令牌ID
    private String tokenId;

    // 用户头像URL
    private String avatar;
    //个人简介
    private String personIntroduction;
    //封面主题
    private Integer theme;

    private Integer playCount;
    private Integer likeCount;
    private Integer fansCount;
    private Integer focusCount;

}
