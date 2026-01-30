package com.easylive.entity.dto;


import com.easylive.constants.Constants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册数据传输对象(DTO)类
 * 用于封装用户注册相关的数据信息
 */

@Data
public class RegisterDTO {

    @NotEmpty
    @Pattern(regexp = Constants.EMAIL_REGEX)
    private String email;

    @NotEmpty
    private String nickName;

    @NotEmpty
    @Size(max = 20)
    @Pattern(regexp = Constants.PASSWORD_REGEX)
    private String registerPassword;

    @NotEmpty
    private String checkCodeKey;

    @NotEmpty
    private String checkCode;

}
