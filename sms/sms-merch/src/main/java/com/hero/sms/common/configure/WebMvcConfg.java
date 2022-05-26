package com.hero.sms.common.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfg implements WebMvcConfigurer {

    @Value("${spring.profiles.save-path}")
    private String configSavePath;
    
    @Value("${spring.profiles.execl-save-path}")
    private String execlSavePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/res/**").addResourceLocations("file:"+configSavePath);
        registry.addResourceHandler("/execlflie/**").addResourceLocations("file:"+execlSavePath);
    }
}