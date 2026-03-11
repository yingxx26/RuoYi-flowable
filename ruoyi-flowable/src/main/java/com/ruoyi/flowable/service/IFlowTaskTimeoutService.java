package com.ruoyi.flowable.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.service.delegate.DelegateTask;


import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 任务超时监控服务接口
 *
 * @author Tony
 * @date 2026-03-10
 */
public interface IFlowTaskTimeoutService {

    /**
     * 注册任务超时监控
     *
     * @param task 任务对象
     */
    void registerTaskTimeout(DelegateTask task);

    /**
     * 取消任务超时监控
     *
     * @param taskId 任务ID
     */
    void cancelTaskTimeout(String taskId);

    /**
     * 注册流程实例超时监控
     *
     * @param execution 执行对象
     */
    void registerProcessTimeout(DelegateExecution execution);

    /**
     * 取消流程实例超时监控
     *
     * @param processInstanceId 流程实例ID
     */
    void cancelProcessTimeout(String processInstanceId);

    /**
     * 扫描超时任务
     * 
     * @param timeoutThreshold 超时阈值（毫秒）
     * @return 超时任务列表
     */
    List<Task> scanTimeoutTasks(long timeoutThreshold);

    /**
     * 扫描超时流程实例
     * 
     * @param timeoutThreshold 超时阈值（毫秒）
     * @return 超时流程实例列表
     */
    List<ProcessInstance> scanTimeoutProcesses(long timeoutThreshold);

    /**
     * 获取任务超时监控映射
     *
     * @return 任务超时监控映射
     */
    Map<String, Date> getTaskTimeoutMap();

    /**
     * 获取流程实例超时监控映射
     *
     * @return 流程实例超时监控映射
     */
    Map<String, Date> getProcessTimeoutMap();
}
