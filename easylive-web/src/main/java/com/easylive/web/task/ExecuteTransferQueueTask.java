package com.easylive.web.task;


import com.easylive.component.RedisComponent;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.service.VideoInfoFilePostService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
public class ExecuteTransferQueueTask {

    private static final Logger log = LoggerFactory.getLogger(ExecuteTransferQueueTask.class);
    @Resource
    private RedisComponent redisComponent;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "video-transfer-queue-worker");
            thread.setDaemon(true);
            return thread;
        }
    });
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @PostConstruct
    public void executeTransferQueue()
    {
        executorService.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Redis 队列没数据时阻塞等待，避免线程反复 wake up 去空查 Redis。
                    VideoInfoFilePost transferVideo = redisComponent.getTransferVideoInfo4QueueBlock();
                    if (transferVideo == null) {
                        continue;
                    }
                    try {
                        videoInfoFilePostService.transferVideo(transferVideo);
                    } catch (Exception e) {
                        // 单个文件转码失败只记日志，消费线程继续处理后面的任务。
                        log.error("文件转码失败, fileId: {}, videoId: {}", transferVideo.getFileId(), transferVideo.getVideoId(), e);
                    }
                }
            } catch (Exception e) {
                log.error("转码队列消费线程异常退出", e);
                Thread.currentThread().interrupt();
            }
        });
    }

}
