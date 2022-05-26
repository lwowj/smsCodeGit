package com.hero.sms.common.configure;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.Connector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumerScheduleService;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import com.hero.sms.service.mq.MQService;
import com.zx.sms.connect.manager.EndpointManager;

import lombok.extern.slf4j.Slf4j;

@Configuration
public class CbShutdownConfig {

    public static final int waitTime = 30;

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(gracefulShutdown());
        return tomcat;
    }

    @Slf4j
    private static class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

        private volatile Connector connector;

        @Override
        public void customize(Connector connector) {
            this.connector = connector;
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            log.info("application is going to stop. try to stop tomcat gracefully");
            this.connector.pause();
            List<MQProducer> producers = MQService.getAllProducer();
            if(CollectionUtils.isNotEmpty(producers)) {
            	for (MQProducer producer : producers) {
            		try {
						producer.shutdown();
					} catch (Exception e) {
						log.error("MQ PRODUCER STOP ERR："+producer.toString(),e);
					}
				}
            }
            List<MQConsumer> consumers = MQService.getAllConsumer();
            if(CollectionUtils.isNotEmpty(consumers)) {
            	for (MQConsumer consumer : consumers) {
            		try {
						if(consumer instanceof MQPushConsumer) {
							((MQPushConsumer) consumer).shutdown();
						}
						if(consumer instanceof MQPullConsumer) {
							((MQPullConsumer) consumer).shutdown();
						}
					} catch (Exception e) {
						log.error("MQ CONSUMER STOP ERR："+consumer.toString(),e);
					}
				}
            }
            List<MQPullConsumerScheduleService> consumerSchedules = MQService.getAllPullConsumerSchedule();
            if(CollectionUtils.isNotEmpty(consumerSchedules)) {
            	for (MQPullConsumerScheduleService consumer : consumerSchedules) {
            		try {
						consumer.shutdown();
					} catch (Exception e) {
						log.error("MQ CONSUMER1 STOP ERR："+consumer.toString(),e);
					}
				}
            }
            try {
				EndpointManager.INS.close();
			} catch (Exception e) {
				log.error("SMSGATE STOP ERR",e);
			}
            Executor executor = this.connector.getProtocolHandler().getExecutor();
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                threadPoolExecutor.shutdown();
                try {
                    if (!threadPoolExecutor.awaitTermination(waitTime, TimeUnit.SECONDS)) {
                        log.info("Tomcat did not terminate in the specified time.");
                        threadPoolExecutor.shutdownNow();
                    }
                } catch (Exception ex) {
                    log.error("awaitTermination failed.", ex);
                    threadPoolExecutor.shutdownNow();
                }
            }
        }
    }
}
