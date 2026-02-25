package com.easylive.web.controller;


import com.easylive.component.RedisComponent;
import com.easylive.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 2026.2.10
 * @author Amani
 */
@RestController
@RequestMapping("/sysSetting")
@Validated
public class SysSettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;


    @RequestMapping("/getSetting")
    public ResponseVO getSetting()
    {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }
}
