package com.easylive.admin.controller;

import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.SysSettingDTO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author amani
 * @since 2026.4.22
 */
@RestController
@RequestMapping("/setting")
public class SettingController extends ABaseController{

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/getSetting")
    public ResponseVO getSetting()
    {
        SysSettingDTO sysSetting = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSetting);
    }

    @RequestMapping("/saveSetting")
    public ResponseVO saveSetting(SysSettingDTO sysSettingDTO)
    {
        boolean ans = redisComponent.setSysSetting(sysSettingDTO);
        if (!ans)
            throw new BusinessException("保存失败");
        return getSuccessResponseVO(null);
    }
}
