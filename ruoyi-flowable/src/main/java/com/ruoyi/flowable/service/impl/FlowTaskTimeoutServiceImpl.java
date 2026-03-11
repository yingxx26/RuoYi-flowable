package com.ruoyi.flowable.service.impl;

import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.service.IFlowTaskTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务超时监控服务
 *
 * @author Tony
 * @date 2026-03-10
 */
@Service
@Slf4j
public class FlowTaskTimeoutServiceImpl extends FlowServiceFactory implements IFlowTaskTimeoutService {

    /**
     * 任务超时监控映射
     * key: taskId
     * value: 任务创建时间
     */
    private final Map<String, Date> taskTimeoutMap = new ConcurrentHashMap<>();

    /**
     * 流程实例超时监控映射
     * key: processInstanceId
     * value: 流程启动时间
     */
    private final Map<String, Date> processTimeoutMap = new ConcurrentHashMap<>();

    /**
     * 注册任务超时监控
     *
     * @param task 任务对象
     */
    public void registerTaskTimeout(DelegateTask task) {
        String taskId = task.getId();
        Date createTime = task.getCreateTime();
        taskTimeoutMap.put(taskId, createTime);
        log.info("注册任务超时监控: taskId={}, createTime={}", taskId, createTime);
    }

    /**
     * 取消任务超时监控
     *
     * @param taskId 任务ID
     */
    public void cancelTaskTimeout(String taskId) {
        if (taskTimeoutMap.containsKey(taskId)) {
            taskTimeoutMap.remove(taskId);
            log.info("取消任务超时监控: taskId={}", taskId);
        }
    }

    /**
     * 注册流程实例超时监控
     *
     * @param execution 执行对象
     */
    public void registerProcessTimeout(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Date startTime = new Date();
        processTimeoutMap.put(processInstanceId, startTime);
        log.info("注册流程实例超时监控: processInstanceId={}, startTime={}", processInstanceId, startTime);
    }

    /**
     * 取消流程实例超时监控
     *
     * @param processInstanceId 流程实例ID
     */
    public void cancelProcessTimeout(String processInstanceId) {
        if (processTimeoutMap.containsKey(processInstanceId)) {
            processTimeoutMap.remove(processInstanceId);
            log.info("取消流程实例超时监控: processInstanceId={}", processInstanceId);
        }
    }

    /**
     * 扫描超时任务
     * 
     * @param timeoutThreshold 超时阈值（毫秒）
     * @return 超时任务列表
     */
    public List<Task> scanTimeoutTasks(long timeoutThreshold) {
        List<Task> timeoutTasks = new ArrayList<>();
        Date now = new Date();
        
        for (Map.Entry<String, Date> entry : taskTimeoutMap.entrySet()) {
            String taskId = entry.getKey();
            Date createTime = entry.getValue();
            
            // 检查任务是否仍然存在
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task != null) {
                long duration = now.getTime() - createTime.getTime();
                if (duration > timeoutThreshold) {
                    timeoutTasks.add(task);
                    log.warn("发现超时任务: taskId={}, taskName={}, duration={}ms", 
                            taskId, task.getName(), duration);
                }
            } else {
                // 任务不存在，从监控映射中移除
                taskTimeoutMap.remove(taskId);
                log.info("任务已不存在，移除监控: taskId={}", taskId);
            }
        }
        
        return timeoutTasks;
    }

    /**
     * 扫描超时流程实例
     * 
     * @param timeoutThreshold 超时阈值（毫秒）
     * @return 超时流程实例列表
     */
    public List<ProcessInstance> scanTimeoutProcesses(long timeoutThreshold) {
        List<ProcessInstance> timeoutProcesses = new ArrayList<>();
        Date now = new Date();
        
        for (Map.Entry<String, Date> entry : processTimeoutMap.entrySet()) {
            String processInstanceId = entry.getKey();
            Date startTime = entry.getValue();
            
            // 检查流程实例是否仍然存在
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (processInstance != null) {
                long duration = now.getTime() - startTime.getTime();
                if (duration > timeoutThreshold) {
                    timeoutProcesses.add(processInstance);
                    log.warn("发现超时流程实例: processInstanceId={}, duration={}ms", 
                            processInstanceId, duration);
                }
            } else {
                // 流程实例不存在，从监控映射中移除
                processTimeoutMap.remove(processInstanceId);
                log.info("流程实例已不存在，移除监控: processInstanceId={}", processInstanceId);
            }
        }
        
        return timeoutProcesses;
    }

    /**
     * 获取任务超时监控映射
     *
     * @return 任务超时监控映射
     */
    public Map<String, Date> getTaskTimeoutMap() {
        return taskTimeoutMap;
    }

    /**
     * 获取流程实例超时监控映射
     *
     * @return 流程实例超时监控映射
     */
    public Map<String, Date> getProcessTimeoutMap() {
        return processTimeoutMap;
    }
}
