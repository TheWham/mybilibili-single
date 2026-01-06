package com.easylive.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EasyliveAdminController {
    @RequestMapping("/test")
    public String test(){
        return "admin模块初始化成功";
    }
}
