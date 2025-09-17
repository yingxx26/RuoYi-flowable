package com.ruoyi.flowable.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "com.ruoyi",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.ruoyi.framework.config..*")
        })
@MapperScan("com.ruoyi.**.mapper")
public class StarterConfig {

}

