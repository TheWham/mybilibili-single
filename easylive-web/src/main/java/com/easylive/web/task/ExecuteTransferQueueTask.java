package com.easylive.web.task;


import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.service.VideoInfoFilePostService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExecuteTransferQueueTask {

    private static final Logger log = LoggerFactory.getLogger(ExecuteTransferQueueTask.class);
    @Resource
    private RedisComponent redisComponent;
    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.LENGTH_2);
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

   // @PostConstruct
    public void executeTransferQueue()
    {
        executorService.execute(()->{
            try {
                while (true)
                {
                    VideoInfoFilePost transferVideo = redisComponent.getTransferVideoInfo4Queue();
                    if (transferVideo==null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    videoInfoFilePostService.transferVideo(transferVideo);
                }
            }catch (InterruptedException e) {
                log.error("文件转码失败 {}", e);

            }
        });
    }
}
