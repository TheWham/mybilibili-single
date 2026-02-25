package com.easylive.web.controller;


import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.VideoInfoPostDTO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.VideoInfoPostService;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ucenter")

public class UCenterController extends ABaseController{

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoPostService videoInfoPostService;



    @RequestMapping("/postVideo")
    @Transactional
    public ResponseVO postVideo(@Validated VideoInfoPostDTO videoInfoPostDTO)
    {
        videoInfoPostDTO.setUserId(getTokenUserInfo().getUserId());
        videoInfoPostService.savePostVideoInfo(videoInfoPostDTO);
        return getSuccessResponseVO(null);
    }

}
