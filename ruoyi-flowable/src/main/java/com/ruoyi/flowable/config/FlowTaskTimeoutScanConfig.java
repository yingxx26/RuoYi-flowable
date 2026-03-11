package com.ruoyi.flowable.config;

import com.ruoyi.flowable.service.IFlowTaskTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

/**
 * 任务超时扫描配置
 *
 * @author Tony
 * @date 2026-03-10
 */
@Configuration
@EnableScheduling
@Slf4j
public class FlowTaskTimeoutScanConfig {

    @Resource
    private IFlowTaskTimeoutService flowTaskTimeoutService;

    /**
     * 定时扫描超时任务
     * 每5分钟扫描一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void scanTimeoutTasks() {
        log.info("开始扫描超时任务...");
        // 设置超时阈值为30分钟（1800000毫秒）
        long timeoutThreshold = 30 * 60 * 1000;
        flowTaskTimeoutService.scanTimeoutTasks(timeoutThreshold);
        log.info("超时任务扫描完成");
    }

    /**
     * 定时扫描超时流程实例
     * 每10分钟扫描一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void scanTimeoutProcesses() {
        log.info("开始扫描超时流程实例...");
        // 设置超时阈值为2小时（7200000毫秒）
        long timeoutThreshold = 2 * 60 * 60 * 1000;
        flowTaskTimeoutService.scanTimeoutProcesses(timeoutThreshold);
        log.info("超时流程实例扫描完成");
    }
}
