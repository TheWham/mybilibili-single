package com.easylive.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务单独走一套调度线程池，不和普通 @Async 线程池混用。
 * 这样短周期同步任务和凌晨统计任务可以并行一些，避免全挤在默认单线程调度器里。
 */
@Configuration
public class ScheduledTaskConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 够让几个 10 秒同步任务并行，避免同时压太多数据库写入。
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scheduled-task-");
        // 应用关闭时把还没跑完的任务等一会儿，减少中途停机留下半截同步的概率。
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }
}
