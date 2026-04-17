package com.easylive.admin.service.impl;

import com.easylive.admin.service.AdminAccountService;
import com.easylive.component.RedisComponent;
import com.easylive.config.AdminConfig;
import com.easylive.entity.dto.AdminLoginDTO;
import com.easylive.exception.BusinessException;
import com.easylive.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AdminConfig adminConfig;

    @Override
    public Map<String, String> getCheckCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCode = captcha.toBase64();
        String checkCodeKey = redisComponent.saveCode(code);

        Map<String, String> result = new HashMap<>(2);
        result.put("checkCodeKey", checkCodeKey);
        result.put("checkCode", checkCode);
        return result;
    }

    @Override
    public String login(AdminLoginDTO adminLoginDTO) {
        String redisCode = redisComponent.getCode(adminLoginDTO.getCheckCodeKey());
        if (!adminLoginDTO.getCheckCode().equalsIgnoreCase(redisCode)) {
            throw new BusinessException("图形验证码不正确");
        }

        // 后台账号是单账号配置模式，这里直接和配置文件里的账号密码做比对。
        // 密码入参已经是前端做过 md5 的值，所以服务端这里继续按配置值做同样的 md5 后再比较。
        if (!adminLoginDTO.getAccount().equals(adminConfig.getAccount())
                || !adminLoginDTO.getPassword().equals(StringTools.md5Password(adminConfig.getPassword()))) {
            throw new BusinessException("账号或密码错误");
        }
        return redisComponent.saveToken4Admin(adminLoginDTO.getAccount());
    }
}
