package com.easylive.admin.controller;


import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.AdminLoginDTO;
import com.easylive.entity.vo.ResponseVO;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController{

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    public RedisComponent redisComponent;

/**
 * 获取验证码接口
 * @return 返回包含验证码图片和验证码key的响应对象
 */
    @RequestMapping("/checkCode")
    public ResponseVO getCheckCode(){
        // 1. 生成验证码
    // 创建一个指定宽高的算术验证码对象
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);

    // 获取验证码文本结果
        String code = captcha.text();
    // 将验证码图片转换为Base64编码格式
        String checkCode = captcha.toBase64();

    // 将验证码保存到Redis中并获取对应的key
        String checkCodeKey = redisComponent.saveCode(code);


    // 创建Map对象用于存储验证码key和验证码图片
        Map<String, String> map = new HashMap<>();
        map.put("checkCodeKey", checkCodeKey); // 存储验证码在Redis中的key
        map.put("checkCode", checkCode);       // 存储验证码图片的Base64编码

    // 返回成功响应，包含验证码相关信息
        return getSuccessResponseVO(map);
    }

    @RequestMapping("/login")
    public ResponseVO login(
            HttpServletResponse response,
            AdminLoginDTO adminLoginDTO)
    {
        try {
            validAdminInfo(response, adminLoginDTO);
            //TODO 设置粉丝数, 关注数, 硬币数
            return getSuccessResponseVO(adminLoginDTO.getAccount());
        }finally {
            redisComponent.cleanCheckCode(adminLoginDTO.getCheckCodeKey());
        }
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response){
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

}
