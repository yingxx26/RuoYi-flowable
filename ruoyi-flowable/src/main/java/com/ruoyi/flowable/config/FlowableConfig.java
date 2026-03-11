package com.ruoyi.flowable.config;

import com.ruoyi.flowable.listener.GlobalTaskEventListener;
import org.flowable.engine.impl.db.DbIdGenerator;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展流程配置
 *
 * @author Tony
 * @date 2022-12-26 10:24
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {


    @Autowired
    private GlobalTaskEventListener globalTaskEventListener;

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        engineConfiguration.setActivityFontName("宋体");
        engineConfiguration.setLabelFontName("宋体");
        engineConfiguration.setAnnotationFontName("宋体");
        engineConfiguration.setIdGenerator(new DbIdGenerator());

        // 注册全局事件监听器
        List eventListeners = new ArrayList<>();
        eventListeners.add(globalTaskEventListener);
        engineConfiguration.setEventListeners(eventListeners);
    }

}
