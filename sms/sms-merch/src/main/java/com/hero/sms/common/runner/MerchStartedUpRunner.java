package com.hero.sms.common.runner;

import java.net.InetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.properties.FebsProperties;
import com.hero.sms.common.service.RedisService;
import com.hero.sms.service.message.ISensitiveWordService;
import com.hero.sms.service.mq.MQService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Administrator
 * @author FiseTch
 */
@Slf4j
@Component
public class MerchStartedUpRunner implements ApplicationRunner {

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private FebsProperties febsProperties;
    @Autowired
    private RedisService redisService;
    @Autowired
    private DatabaseCache databaseCache;
    @Autowired
    private ISensitiveWordService sensitiveWordService;
    @Autowired
    private MQService mqService;

    @Value("${server.port:8070}")
    private String port;
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    @Value("${spring.profiles.active}")
    private String active;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //加载缓存
        try {
            databaseCache.init();
        } catch (Exception e) {
            log.error(" ____   __    _   _ ");
            log.error("| |_   / /\\  | | | |");
            log.error("|_|   /_/--\\ |_| |_|__");
            log.error("                        ");
            log.error("缓存加载失败，{}", e);
            log.error("缓存加载失败，请检查");
            context.close();
            return;
        }
        
      //加载敏感词
        try {
        	sensitiveWordService.init();
        } catch (Exception e) {
        	log.error(" ____   __    _   _ ");
        	log.error("| |_   / /\\  | | | |");
        	log.error("|_|   /_/--\\ |_| |_|__");
        	log.error("                        ");
        	log.error("敏感词加载失败，{}", e);
        	log.error("敏感词加载失败，请检查");
        	context.close();
        	return;
        }

        try {
            // 测试 Redis连接是否正常
            redisService.hasKey("febs_test");
        } catch (Exception e) {
            log.error(" ____   __    _   _ ");
            log.error("| |_   / /\\  | | | |");
            log.error("|_|   /_/--\\ |_| |_|__");
            log.error("                        ");
            log.error("短信平台启动失败，{}", e);
            log.error("Redis连接异常，请检查Redis连接配置并确保Redis服务已启动");
            // 关闭 FEBS
            context.close();
        }
        try {
            mqService.initSendBoxProducer();
			mqService.initHttpNotifyOrgConsumer();
        } catch (Exception e) {
            log.error(" ____   __    _   _ ");
            log.error("| |_   / /\\  | | | |");
            log.error("|_|   /_/--\\ |_| |_|__");
            log.error("                        ");
            log.error("短信网关启动失败，{}", e);
            log.error("MQ创建失败，请检查下连接配置");
            context.close();
            return;
        }
        if (context.isActive()) {
            InetAddress address = InetAddress.getLocalHost();
            String url = String.format("http://%s:%s", address.getHostAddress(), port);
            String loginUrl = febsProperties.getShiro().getLoginUrl();
            if (StringUtils.isNotBlank(contextPath))
                url += contextPath;
            if (StringUtils.isNotBlank(loginUrl))
                url += loginUrl;
            log.info(" __    ___   _      ___   _     ____ _____  ____ ");
            log.info("/ /`  / / \\ | |\\/| | |_) | |   | |_   | |  | |_  ");
            log.info("\\_\\_, \\_\\_/ |_|  | |_|   |_|__ |_|__  |_|  |_|__ ");
            log.info("                                                      ");
            log.info("短信平台-商户端启动完毕，地址：{}", url);

            /*boolean auto = febsProperties.isAutoOpenBrowser();
            if (auto && StringUtils.equalsIgnoreCase(active, "dev")) {
                String os = System.getProperty("os.name");
                // 默认为 windows时才自动打开页面
                if (StringUtils.containsIgnoreCase(os, "windows")) {
                    //使用默认浏览器打开系统登录页
                    Runtime.getRuntime().exec("cmd  /c  start " + url);
                }
            }*/
        }
    }
}
