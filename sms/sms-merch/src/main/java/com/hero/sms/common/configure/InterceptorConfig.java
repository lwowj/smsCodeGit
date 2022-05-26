package com.hero.sms.common.configure;

import com.hero.sms.common.interceptor.DomainInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private DomainInterceptor domainInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 通过registry来注册拦截器，通过addPathPatterns来添加拦截路径
        registry.addInterceptor(this.domainInterceptor).addPathPatterns("/login")
                .addPathPatterns("/index")
                .addPathPatterns("/febs/views/*");
    }

}
