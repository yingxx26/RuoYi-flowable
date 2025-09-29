package com.ruoyi.flowable.config;

import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.flowable.spring.job.service.SpringAsyncExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimerConfig {

    @Bean
    public SpringAsyncExecutor springAsyncExecutor() {
        return new SpringAsyncExecutor();
    }

    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer(SpringAsyncExecutor springAsyncExecutor) {
        return processEngineConfiguration -> {
            processEngineConfiguration.setAsyncExecutor(springAsyncExecutor);
            processEngineConfiguration.setAsyncExecutorActivate(true);
        };
    }

}
