package com.hero.sms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Administrator
 */
@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.hero.sms.*.mapper")
@MapperScan("com.hero.sms.mapper")
public class SmsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SmsApplication.class).run(args);
    }

}
