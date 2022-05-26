package com.hero.sms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Administrator
 */
@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.hero.sms.mapper")
public class AgentApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AgentApplication.class).run(args);
    }

}
