package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自定义定时任务类
 */
@Component
@Slf4j
public class MyTask {

//    @Scheduled(cron = "1/5 * * * * ? ")
    public void testSchedule(){
        log.info("hahaha");
    }
}
