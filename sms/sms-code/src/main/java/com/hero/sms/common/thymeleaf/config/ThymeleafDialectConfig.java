package com.hero.sms.common.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hero.sms.common.thymeleaf.dialect.SysDialect;

@Configuration
public class ThymeleafDialectConfig {
    /**
     * 系统方言
     * 主要作用有：
     * 1. 处理字典数据展示
     *
     * @return
     */
    @Bean
    public SysDialect sysDialect() {
        return new SysDialect();
    }
}
