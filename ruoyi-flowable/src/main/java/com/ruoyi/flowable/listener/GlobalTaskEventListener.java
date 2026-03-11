package com.ruoyi.flowable.listener;

import com.ruoyi.flowable.service.IFlowTaskTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自定义用户任务解析器，为所有用户任务添加监听器
 */
@Component
@Slf4j
public class GlobalTaskEventListener implements FlowableEventListener {

    @Autowired
    private IFlowTaskTimeoutService flowTaskTimeoutService;

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEventType eventType = event.getType();

        if (event instanceof FlowableEntityEventImpl) {
            FlowableEntityEventImpl entityEvent = (FlowableEntityEventImpl) event;
            if (entityEvent.getEntity() instanceof TaskEntity) {
                TaskEntity taskEntity = (TaskEntity) entityEvent.getEntity();

                // 任务创建
                if (FlowableEngineEventType.TASK_CREATED.equals(eventType)) {
                    log.info("全局监听器 - 任务创建: {}", taskEntity.getId());
                    // 将 TaskEntity 转换为 DelegateTask
                    // TaskEntity 已经实现了 DelegateTask 接口，可以直接转换
                    DelegateTask delegateTask = (DelegateTask) taskEntity;
                    // 注册超时监控
                    flowTaskTimeoutService.registerTaskTimeout(delegateTask);

                }
                // 任务完成
                else if (FlowableEngineEventType.TASK_COMPLETED.equals(eventType)) {
                    log.info("全局监听器 - 任务完成: {}", taskEntity.getId());
                    flowTaskTimeoutService.cancelTaskTimeout(taskEntity.getId());
                }
                /*// 任务删除
                else if (FlowableEngineEventType.TAS.TASK_DELETED.equals(eventType)) {
                    log.info("全局监听器 - 任务删除: {}", taskEntity.getId());
                    flowTaskTimeoutService.cancelTaskTimeout(taskEntity.getId());
                }*/
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }

}