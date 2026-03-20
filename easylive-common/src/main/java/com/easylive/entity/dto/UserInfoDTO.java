package com.easylive.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserInfoDTO {
    @NotEmpty
    @Size(max = 20)
    private String nickName;
    @NotEmpty
    @Size(max = 100)
    private String avatar;
    @NotNull
    private Integer sex;
    @NotEmpty
    private String userId;
    @Size(max = 10)
    private String birthday;
    @Size(max = 150)
    private String school;
    @Size(max = 80)
    private String personIntroduction;
    @Size(max = 300)
    private String noticeInfo;

}
