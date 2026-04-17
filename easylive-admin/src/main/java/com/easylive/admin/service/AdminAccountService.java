package com.easylive.admin.service;

import com.easylive.entity.dto.AdminLoginDTO;

import java.util.Map;

/**
 * 后台账号相关服务。
 * 这里统一承接验证码生成、登录校验这类和控制器无关的业务逻辑，
 * 避免 controller 基类继续堆登录细节。
 */
public interface AdminAccountService {

    /**
     * 生成后台登录验证码，并返回验证码图片和对应的 Redis key。
     */
    Map<String, String> getCheckCode();

    /**
     * 校验后台登录信息，成功后返回新生成的 tokenId。
     */
    String login(AdminLoginDTO adminLoginDTO);
}
