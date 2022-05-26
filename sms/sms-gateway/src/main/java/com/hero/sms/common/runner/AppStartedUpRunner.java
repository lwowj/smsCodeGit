package com.hero.sms.common.runner;

import java.net.InetAddress;

import com.zx.sms.connect.manager.EndpointManager;
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
import com.hero.sms.service.mq.SmsGateService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Administrator
 * @author FiseTch
 */
@Slf4j
@Component
public class AppStartedUpRunner implements ApplicationRunner {

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private FebsProperties febsProperties;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MQService mqService;
    @Autowired
    private SmsGateService SmsGateService;
    @Autowired
    private DatabaseCache databaseCache;
    @Autowired
    private ISensitiveWordService sensitiveWordService;

    @Value("${server.port:8080}")
    private String port;
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${gatewayType}")
    private Integer gatewayType;

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
            log.error("短信网关启动失败，{}", e);
            log.error("Redis连接异常，请检查Redis连接配置并确保Redis服务已启动");
            // 关闭 FEBS
            context.close();
            return;
        }

        if(gatewayType != null && (gatewayType&1) == 1) {
        	try {
        		//1-1 初始化发件箱生产者
        		mqService.initSendBoxProducer();
        		//2-1 初始化发件箱消费者
        		mqService.initSendBoxConsumer();
        		//5-1 初始化发送记录消费者（HTTP）
        		mqService.initSendRecordHttpConsumer();
        		//5-1 初始化通知商户生产
        		mqService.initNotifyOrgProducer();
        		//初始化通知商户消费者（HTTP）
        		mqService.initHttpNotifyOrgConsumer();
        		//4-1 初始化回执处理结果消费者
        		mqService.initUpstreamReceiptConsumer();
        	} catch (Exception e) {
        		log.error(" ____   __    _   _ ");
        		log.error("| |_   / /\\  | | | |");
        		log.error("|_|   /_/--\\ |_| |_|__");
        		log.error("                        ");
        		log.error("短信网关启动失败，MQ创建失败：{}", e);
        		context.close();
        		return;
        	}
        }

        if(gatewayType != null && (gatewayType&2) == 2) {
        	try {
        		//6-1 初始化smmp接口服务端(主要提供给下游客户调用及上游回执通知过来的调用)
        		SmsGateService.initGateServer();
        	} catch (Exception e) {
        		log.error(" ____   __    _   _ ");
        		log.error("| |_   / /\\  | | | |");
        		log.error("|_|   /_/--\\ |_| |_|__");
        		log.error("                        ");
        		log.error("短信网关启动失败，SMPP创建下游连接失败：{}", e);
        		context.close();
        		return;
        	}
        }

        if(gatewayType != null && (gatewayType&4) == 4) {
        	try {
        		//4-1 初始化回执处理生产者
        		mqService.initUpstreamReceiptProducer();
        		//初始化smpp接客户端（主要用于请求上游短信接口及 短信结果通知下游SMPP商户）
        		//3-1 内含初始化 各个上游通道的分拣结果处理消费者
				SmsGateService.initGateClient();
				//检索smpp客户端连接是否正常
                EndpointManager.INS.startConnectionCheckTask();
			} catch (Exception e) {
				log.error(" ____   __    _   _ ");
	            log.error("| |_   / /\\  | | | |");
	            log.error("|_|   /_/--\\ |_| |_|__");
	            log.error("                        ");
	            log.error("短信网关启动失败,smpp创建上游连接失败：{}", e);
	            context.close();
			}
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
            log.info("网关平台系统启动完毕，地址：{}", url);
        }
    }
}
