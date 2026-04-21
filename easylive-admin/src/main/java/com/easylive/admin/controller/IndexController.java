package com.easylive.admin.controller;

import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.AdminStatsTypeEnum;
import com.easylive.service.UserStatsService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author amani
 * @since 2026.4.22
 */
@RestController
@Validated
@RequestMapping("index")
public class IndexController extends ABaseController{

    @Resource
    private UserStatsService userStatsService;


    @RequestMapping("getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo()
    {
        return getSuccessResponseVO(userStatsService.getAdminActualTimeStatisticsInfo());
    }

    @RequestMapping("getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo(@NotNull Integer dataType)
    {
        AdminStatsTypeEnum statsTypeEnum = AdminStatsTypeEnum.getEnum(dataType);
        return getSuccessResponseVO(userStatsService.getAdminWeekStatisticsInfo(statsTypeEnum));
    }
}
