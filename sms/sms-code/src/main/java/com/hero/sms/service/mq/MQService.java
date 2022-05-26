package com.hero.sms.service.mq;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.entity.message.*;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.enums.channel.SmsChannelProtocolTypeEnums;
import com.hero.sms.enums.channel.SmsChannelPushPropertyNameEnums;
import com.hero.sms.enums.organization.OrganizationPropertyNameEnums;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordService;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.smpp.SMPPServerEndpointEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.*;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MQService {
	
	enum ProducerEnum{SendBoxP,UpstreamReceiptP,NotifyOrgP}
	enum consumerEnum{SendBoxC,SendRecordHttpC,SendRecordSMSGATEC,UpstreamReceiptC,HttpNotifyOrgC,GateNotifyOrgC}
	
    @Value("${rocketMQ.nameServerAddr}")
    private String rocketMQNameSrvAddr;
    @Value("${localIp}")
    private String localIp;
    
	@Lazy
    @Autowired
    private ISendBoxService sendBoxService;

	@Lazy
    @Autowired
    private ISendRecordService sendRecordService;
    
    @Autowired
    private IReturnRecordService returnRecordService;
    
    private static Map<String,MQProducer> producerMap;
    
    private static Map<String,MQConsumer> consumerMap;

    private static Map<String,MQPullConsumerScheduleService> pullConsumerScheduleServiceMap;
    
    public static Map<String,MQProducer> getSingleMQProducer(){
    	if(producerMap == null) {
    		producerMap = Maps.newHashMap();
    	}
    	return producerMap;
    }

    public static Map<String,MQConsumer> getSingleMQConsumer(){
    	if(consumerMap == null) {
    		consumerMap = Maps.newHashMap();
    	}
    	return consumerMap;
    }
    
    public static Map<String,MQPullConsumerScheduleService> getSingleMQPullConsumerSchedule(){
    	if(pullConsumerScheduleServiceMap == null) {
    		pullConsumerScheduleServiceMap = Maps.newHashMap();
    	}
    	return pullConsumerScheduleServiceMap;
    }
    
    public static MQProducer getProducer(String name) {
    	return producerMap.get(name);
    }
    
    public static MQConsumer getConsumer(String name) {
    	return consumerMap.get(name);
    }

    public static MQPullConsumerScheduleService getPullConsumerSchedule(String name) {
    	return pullConsumerScheduleServiceMap.get(name);
    }
    
    public static List<MQProducer> getAllProducer(){
    	if(producerMap != null) {
    		return producerMap.values().stream().collect(Collectors.toList());
    	}
    	return null;
    }

    public static List<MQConsumer> getAllConsumer(){
    	if(consumerMap != null) {
    		return consumerMap.values().stream().collect(Collectors.toList());
    	}
    	return null;
    }
    
    public static List<MQPullConsumerScheduleService> getAllPullConsumerSchedule(){
    	if(pullConsumerScheduleServiceMap != null) {
    		return pullConsumerScheduleServiceMap.values().stream().collect(Collectors.toList());
    	}
    	return null;
    }

    /**
     * 初始化发件箱生产者
     * @throws MQClientException
     */
    public void initSendBoxProducer() throws MQClientException{
    	String key = ProducerEnum.SendBoxP.name();
    	DefaultMQProducer producer = new DefaultMQProducer("sendSMSGroup");
    	producer.setNamesrvAddr(rocketMQNameSrvAddr);
    	//设置 lnstanceName，当一个 Jvm 需要启动多个 Producer 的时候，通过设置不同的 InstanceName来区分，不设置的话系统使用默认名称“DEFAULT”。
    	producer.setInstanceName(key+localIp);
		//设置发送失败重试次数
    	producer.setRetryTimesWhenSendFailed(0);
    	producer.setVipChannelEnabled(false);
    	producer.setSendMsgTimeout(10000);
        //初始化 Producer，整个应用生命周期内只需要初始化一次
    	producer.start();
    	getSingleMQProducer().put(key, producer);
    }

    /**
     * 初始化回执生产者
     * @throws MQClientException
     */
    public void initUpstreamReceiptProducer() throws MQClientException{
    	String key = ProducerEnum.UpstreamReceiptP.name();
    	DefaultMQProducer producer = new DefaultMQProducer("upstreamReceiptGroup");
    	producer.setNamesrvAddr(rocketMQNameSrvAddr);
    	producer.setInstanceName(key+localIp);
		//设置发送失败重试次数
    	producer.setRetryTimesWhenSendFailed(0);
    	producer.setVipChannelEnabled(false);
		producer.setSendMsgTimeout(10000);
    	//初始化 Producer，整个应用生命周期内只需要初始化一次
    	producer.start();
    	getSingleMQProducer().put(key, producer);
    }

    /**
     * 初始化通知商户生产者
     * @throws MQClientException
     */
    public void initNotifyOrgProducer() throws MQClientException{
    	String key = ProducerEnum.NotifyOrgP.name();
    	DefaultMQProducer producer = new DefaultMQProducer("notifyOrgGroup");
    	producer.setNamesrvAddr(rocketMQNameSrvAddr);
    	producer.setInstanceName(key+localIp);
    	//设置发送失败重试次数
    	producer.setRetryTimesWhenSendFailed(0);
    	producer.setVipChannelEnabled(false);
		producer.setSendMsgTimeout(10000);
    	//初始化 Producer，整个应用生命周期内只需要初始化一次
    	producer.start();
    	getSingleMQProducer().put(key, producer);
    }
    
    /**
     * 初始化发件箱分拣消费者
     * @throws MQClientException
     */
    public void initSendBoxConsumer() throws MQClientException {
    	String key = consumerEnum.SendBoxC.name();
    	// 用于把多个Consumer组织到一起，提高并发处理能力
    	DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("SortingGroupName");
    	// 设置nameServer地址，多个以;分隔
    	consumer.setNamesrvAddr(rocketMQNameSrvAddr);
    	//设置 lnstanceName，当一个 Jvm 需要启动多个 consumer 的时候，通过设置不同的 InstanceName来区分，不设置的话系统使用默认名称“DEFAULT”。
    	consumer.setInstanceName(key+localIp);
    	/**
         * CLUSTERING：默认模式，同一个ConsumerGroup(groupName相同)每个consumer只消费所订阅消息的一部分内容，同一个ConsumerGroup里所有的Consumer消息加起来才是所
                 *  订阅topic整体，从而达到负载均衡的目的
         * BROADCASTING：同一个ConsumerGroup每个consumer都消费到所订阅topic所有消息，也就是一个消费会被多次分发，被多个consumer消费。
         * 
         */
    	consumer.setMessageModel(MessageModel.CLUSTERING);
    	/**
    	  *  设置消费线程数大小取值范围都是 [1, 1000]。
    	 * 4.2版本中的默认配置为：
    	 * consumeThreadMin 默认是20
    	 * consumeThreadMax 默认是64
    	 */
    	consumer.setConsumeThreadMax(40);
    	consumer.setConsumeThreadMin(20);
    	consumer.setVipChannelEnabled(false);
    	 /**
         * 1. CONSUME_FROM_LAST_OFFSET：第一次启动从队列最后位置消费，后续再启动接着上次消费的进度开始消费 
           2. CONSUME_FROM_FIRST_OFFSET：第一次启动从队列初始位置消费，后续再启动接着上次消费的进度开始消费 
           3. CONSUME_FROM_TIMESTAMP：第一次启动从指定时间点位置消费，后续再启动接着上次消费的进度开始消费 
                以上所说的第一次启动是指从来没有消费过的消费者，如果该消费者消费过，那么会在broker端记录该消费者的消费位置，如果该消费者挂了再启动，那么自动从上次消费的进度开始
         */
    	consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    	// 订阅topic，可以对指定消息进行过滤，例如："TopicTest","tagl||tag2||tag3",*或null表示topic所有消息
    	consumer.subscribe(MessageConstant.MQ_Topic_SendBox, MessageConstant.MQ_TAG_SendBox);
    	consumer.registerMessageListener(new MessageListenerConcurrently() {
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
                if (list != null) {
                    for (MessageExt ext : list) {
                    	if(ext.getReconsumeTimes() > 0) {
                    		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    	}
                    	long start = System.currentTimeMillis();
                        try {
	                       	 String body = new String(ext.getBody(), "UTF-8");
	                       	 SimpleNote simpleSendBox = JSON.parseObject(body,SimpleNote.class);
	                       	 String checkData = MD5Util.encrypt(simpleSendBox.getId().toString(), MessageConstant.MQ_MD5_Key);
	                       	 if(!checkData.equals(simpleSendBox.getCheckData())) {
	                       		 log.error("数据校验失败:"+body);
	                       		 continue;
	                       	 }
	                       	List<Object> datas = (List<Object>)simpleSendBox.getData();
	                       	try {
	                       		Date startDate = new Date(start);
	                        	String startDateStr = DateUtil.getString(startDate, DateUtil.Y_M_D_H_M_S_S_1);
	                        	log.info(String.format("[Begin分拣]========【%s】开始时间：【%s】,数量【%d】", ext.getMsgId(),startDateStr,datas.size()));
							} catch (Exception e) {
								// TODO: handle exception
								log.info(String.format("[Begin分拣]========【%s】开始时间：【%d】,数量【%d】", ext.getMsgId(),start,datas.size()));
							}
	                       	//2-2-1 平台进行信息的分拣
	                        sendBoxService.sortingSendBox(simpleSendBox,ext.getMsgId());
                        } catch (Throwable e) {
                            log.error("发件箱消费者获取消息处理失败", e);
                        }
                        long end = System.currentTimeMillis();
                        long getTime = end-start;
                        try {
                        	Date endDate = new Date(end);
                            String endDateStr = DateUtil.getString(endDate, DateUtil.Y_M_D_H_M_S_S_1);
                            log.info(String.format("【End分拣】========【%s】结束时间：【%s】,分拣耗时【%d】ms",ext.getMsgId(),endDateStr,getTime));
						} catch (Exception e) {
							// TODO: handle exception
							log.info(String.format("【End分拣】========【%s】,分拣耗时【%d】ms",ext.getMsgId(),getTime));
						}
                    }
                }

                // ConsumeConcurrentlyStatus.RECONSUME_LATER boker会根据设置的messageDelayLevel发起重试，默认16次
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
    	//通过源码可以看出主要实现过程在DefaultMQPushConsumerImpl类中，consumer.start后调用DefaultMQPushConsumerImpl的同步start方法
    	consumer.start();
    	getSingleMQConsumer().put(key, consumer);
    }
    
    /**
     * 初始化发送记录消费者MQ(http)
     * @throws MQClientException
     */
	public void initSendRecordHttpConsumer() throws MQClientException {
		String key = consumerEnum.SendRecordHttpC.name();
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("PushMsgGroupName");
		consumer.setNamesrvAddr(rocketMQNameSrvAddr);
		consumer.setInstanceName(key+localIp);
		consumer.setMessageModel(MessageModel.CLUSTERING);
		consumer.setConsumeThreadMax(20);
		consumer.setConsumeThreadMin(10);
		consumer.setVipChannelEnabled(false);
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.subscribe(MessageConstant.MQ_Topic_SendRecord+SmsChannelProtocolTypeEnums.Http.getCode(), MessageConstant.MQ_TAG_SendRecord);
		consumer.registerMessageListener(new MessageListenerConcurrently() {
        	 public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
        		 if (list != null) {
        			 for (MessageExt ext : list) {
        				 if(ext.getReconsumeTimes() > 0) return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        				 try {
        					 String body = new String(ext.getBody(), "UTF-8");
        					 SimpleNote simpleSendRecord = JSON.parseObject(body,SimpleNote.class);
        					 String checkData = MD5Util.encrypt(simpleSendRecord.getId().toString(), MessageConstant.MQ_MD5_Key);
        					 if(!checkData.equals(simpleSendRecord.getCheckData())) {
        						 log.error("数据校验失败:"+body);
        						 continue;
        					 }
        					 //调用 http协议 请求上游通道接口
        					 sendRecordService.pushHttpMsg(simpleSendRecord.getId());
        				 } catch (Throwable e) {
        					 log.error("发件箱消费者获取消息处理失败", e);
        				 }
        			 }
        		 }
        		 return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        	 }
         });
		consumer.start();
		getSingleMQConsumer().put(key, consumer);
	}

	public void initSendRecordConsumer(SmsChannelExt smsChannel) throws MQClientException {
		String protocolType = smsChannel.getProtocolType();
		SmsChannelProperty speedProperty = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.Speed.getCode());
		final int getNum = Integer.parseInt(speedProperty.getValue());
		/*DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("smppConsumerGroup"+smsChannel.getId());
		consumer.setNamesrvAddr(rocketMQNameSrvAddr);
		consumer.start();
		Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(MessageConstant.MQ_Topic_SendRecord+SmsChannelProtocolTypeEnums.Smpp.getCode()+smsChannel.getId());
		for (MessageQueue mq : mqs) {
			SINGLE_MQ:
	            while (true) {
	                try {
	                    PullResult pullResult =
	                        consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 32);
	                    System.out.printf("%s%n", pullResult);
	                    putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
	                    switch (pullResult.getPullStatus()) {
	                        case FOUND:
	                            break;
	                        case NO_MATCHED_MSG:
	                            break;
	                        case NO_NEW_MSG:
	                            break SINGLE_MQ;
	                        case OFFSET_ILLEGAL:
	                            break;
	                        default:
	                            break;
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
		}*/
		String channelId = smsChannel.getId().toString();
		String key = consumerEnum.SendRecordSMSGATEC.name()+channelId;
		if(getSingleMQPullConsumerSchedule().get(key) != null) return;
		final MQPullConsumerScheduleService scheduleService = new MQPullConsumerScheduleService(key);
		scheduleService.getDefaultMQPullConsumer().setInstanceName(key+localIp);;
		scheduleService.getDefaultMQPullConsumer().setVipChannelEnabled(false);
		scheduleService.getDefaultMQPullConsumer().setNamesrvAddr(rocketMQNameSrvAddr);
		scheduleService.setMessageModel(MessageModel.CLUSTERING);
		//3-1-2-1 消费者处理 SendBoxP 推送MQ的分拣信息SendRecordGatePullTaskCallback方法
		scheduleService.registerPullTaskCallback(MessageConstant.MQ_Topic_SendRecord+protocolType+channelId,new SendRecordGatePullTaskCallback(protocolType,channelId,getNum));
		scheduleService.registerPullTaskCallback(MessageConstant.MQ_Topic_SendRecord+"_Retry_"+protocolType+channelId,new SendRecordGatePullTaskCallback(protocolType,channelId,getNum));
		scheduleService.start();
		getSingleMQPullConsumerSchedule().put(key, scheduleService);
	}
	
	class SendRecordGatePullTaskCallback implements PullTaskCallback{
		
		private String protocolType;
		
		private String channelId;
		
		private int maxNum;
		
		public SendRecordGatePullTaskCallback(String protocolType,String channelId, int maxNum) {
			super();
			this.protocolType = protocolType;
			this.channelId = channelId;
			this.maxNum = maxNum;
		}

		@Override
		public void doPullTask(MessageQueue mq, PullTaskContext context) {
			MQPullConsumer consumer = context.getPullConsumer();
			try {
				EndpointEntity entity = EndpointManager.INS.getEndpointEntity(channelId);
				if(entity == null || entity.getSingletonConnector() == null) {
					log.info("-------------长连接通道未启动------------------");
					Thread.sleep(10000);
					return;
				}
				if(entity.getSingletonConnector().fetch() == null) {
					log.info("-------------获取不到链接------------------");
					Thread.sleep(1000);
					return;
				}
				log.debug("----进入循环抓取----");
				long offset = consumer.fetchConsumeOffset(mq, false);
				if(offset < 0)
					offset = 0;
				PullResult pullResult = consumer.pullBlockIfNotFound(mq, MessageConstant.MQ_TAG_SendRecord, offset, maxNum);
				consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
				switch (pullResult.getPullStatus()) {
				case FOUND:
					List<MessageExt> messageExtList = pullResult.getMsgFoundList();
					for (MessageExt ext : messageExtList) {
						 if(ext.getReconsumeTimes() > 0) {
							 try {
								String body = new String(ext.getBody(), "UTF-8");
								 log.error(String.format("重复拉取到的消息[%s]：%s",ext.getMsgId(), body));
							} catch (Exception e) {
								log.error(String.format("重复消息[%s]打印失败",ext.getMsgId()));
							}
							 continue;
						 }
        				 try {
        					 String body = new String(ext.getBody(), "UTF-8");
        					 log.info(String.format("收到消息【%s】topic【%s】body【%s】",ext.getMsgId(), ext.getTopic(),body));
        					 SimpleNote simpleSendRecord = JSON.parseObject(body,SimpleNote.class);
        					 String checkData = MD5Util.encrypt(simpleSendRecord.getId().toString(), MessageConstant.MQ_MD5_Key);
        					 if(!checkData.equals(simpleSendRecord.getCheckData())) {
        						 log.error("数据校验失败:"+body);
        						 continue;
        					 }
        					//3-1-2-2 将分拣结果进行向上游请求的操作 （SMPP协议）
        					 sendRecordService.pushMsg(protocolType,simpleSendRecord.getId());
        				 } catch (Throwable e) {
        					 log.error("SMS GATE DOING MSG ERR", e);
        				 }
					}
					break;
				case NO_MATCHED_MSG:
					break;
				case NO_NEW_MSG:
					break;
				case OFFSET_ILLEGAL:
					break;
				default:
					break;
				}
				context.setPullNextDelayTimeMillis(100);
			} catch (Exception e) {
				log.error("SMS GATE PULL MSG ERR",e);
			}
		}
	}
	
	public void initGateNotifyConsumer(Integer type,OrganizationExt organizationExt) throws MQClientException {
		OrganizationProperty readLimitProperty = organizationExt.getProperty(type, OrganizationPropertyNameEnums.ReadLimit.getCode());
		final int getNum = Integer.parseInt(readLimitProperty.getValue());
		String orgCode = organizationExt.getOrganizationCode();
		String key = consumerEnum.GateNotifyOrgC.name()+orgCode+type;
		if(getSingleMQPullConsumerSchedule().get(key) != null) return;
		final MQPullConsumerScheduleService scheduleService = new MQPullConsumerScheduleService(key);
		scheduleService.getDefaultMQPullConsumer().setInstanceName(key+localIp);;
		scheduleService.getDefaultMQPullConsumer().setVipChannelEnabled(false);
		scheduleService.getDefaultMQPullConsumer().setNamesrvAddr(rocketMQNameSrvAddr);
		scheduleService.setMessageModel(MessageModel.CLUSTERING);
		//6-1-2-1 指定下游商户通知消费者接受到信息后，执行的方法 GateNotifyPullTaskCallback
		scheduleService.registerPullTaskCallback(MessageConstant.MQ_Topic_NotifyOrg+orgCode+type,new GateNotifyPullTaskCallback(type,orgCode,getNum));
		scheduleService.start();
		getSingleMQPullConsumerSchedule().put(key, scheduleService);
	}
	
	class GateNotifyPullTaskCallback implements PullTaskCallback{
		
		private Integer type;
		
		private String orgCode;
		
		private int maxNum;
		
		public GateNotifyPullTaskCallback(Integer type,String orgCode, int maxNum) {
			super();
			this.type = type;
			this.orgCode = orgCode;
			this.maxNum = maxNum;
		}

		@Override
		public void doPullTask(MessageQueue mq, PullTaskContext context) {
			MQPullConsumer consumer = context.getPullConsumer();
			try {
				SMPPServerEndpointEntity server = (SMPPServerEndpointEntity)EndpointManager.INS.getEndpointEntity(SmsGateService.smppServerName);
				if(server == null || server.getSingletonConnector() == null) {
					log.debug("-------------smpp服务端未启动------------------");
					Thread.sleep(10000);
					return;
				}
				EndpointEntity child = server.getChild(orgCode,ChannelType.DUPLEX);
				if(child == null) {
					log.debug(String.format("-------------smpp服务端【商户：%s】未启动------------------",orgCode));
					Thread.sleep(1000);
					return;
				}
				log.debug("----进入循环抓取----");
				long offset = consumer.fetchConsumeOffset(mq, false);
				if(offset < 0)
					offset = 0;
				PullResult pullResult = consumer.pullBlockIfNotFound(mq, MessageConstant.MQ_TAG_NotifyOrg+orgCode+type, offset, maxNum);
				consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
				switch (pullResult.getPullStatus()) {
				case FOUND:
					List<MessageExt> messageExtList = pullResult
							.getMsgFoundList();
					for (MessageExt ext : messageExtList) {
						 if(ext.getReconsumeTimes() > 0) {
							 try {
								String body = new String(ext.getBody(), "UTF-8");
								 log.error(String.format("重复拉取到的消息[%s]：%s",ext.getMsgId(), body));
							} catch (Exception e) {
								log.error(String.format("重复消息[%s]打印失败",ext.getMsgId()));
							}
							 continue;
						 }
        				 try {
        					 String body = new String(ext.getBody(), "UTF-8");
        					 log.info(String.format("收到消息【%s】topic【%s】body【%s】",ext.getMsgId(), ext.getTopic(),body));
        					 NotifyMsgStateModel model = JSON.parseObject(body,NotifyMsgStateModel.class);
        					//6-1-2-1-1 最终将短信发送结果以SMPP的方式推送给下游商户
        					 SmsGateService.notify(child,model);
        				 } catch (Throwable e) {
        					 log.error("SMS GATE SERVER NOTIFY MSG ERR", e);
        				 }
					}
					break;
				case NO_MATCHED_MSG:
					break;
				case NO_NEW_MSG:
					break;
				case OFFSET_ILLEGAL:
					break;
				default:
					break;
				}
				context.setPullNextDelayTimeMillis(100);
			} catch (Exception e) {
				log.error("SMS GATE PULL MSG ERR",e);
			}
		}
	}

	/**
     * 初始化回执
     * @throws MQClientException
     */
	public void initUpstreamReceiptConsumer() throws MQClientException {
		String key = consumerEnum.UpstreamReceiptC.name();
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("UpstreamReceiptGroup");
		consumer.setNamesrvAddr(rocketMQNameSrvAddr);
		consumer.setInstanceName(key+localIp);
		consumer.setMessageModel(MessageModel.CLUSTERING);
		consumer.setConsumeThreadMax(20);
		consumer.setConsumeThreadMin(10);
		consumer.setVipChannelEnabled(false);
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.subscribe(MessageConstant.MQ_Topic_UpstreamReceipt, MessageConstant.MQ_TAG_UpstreamReceipt);
		consumer.registerMessageListener(new MessageListenerConcurrently() {
        	 public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
        		 if (list != null) {
        			 for (MessageExt ext : list) {
        				 try {
        					 String body = new String(ext.getBody(), "UTF-8");
        					 SimpleNote simpleSendRecord = JSON.parseObject(body,SimpleNote.class);
        					 String data = simpleSendRecord.getData().toString();
        					 String checkData = MD5Util.encrypt(data, MessageConstant.MQ_MD5_Key);
        					 if(!checkData.equals(simpleSendRecord.getCheckData())) {
        						 log.error("数据校验失败:"+body);
        						 continue;
        					 }
        					//4-1-1 回执处理结果消费者 再接收到 消息后，根据chain-cfg.xml中的配置receiptReturnRecord1，进行相关校验、业务逻辑处理
        					 returnRecordService.receiptReturnRecord(JSON.parseObject(data, ReturnRecord.class),ext.getMsgId());
        				 } catch (Throwable e) {
        					 log.error("回执消费者获取消息处理失败", e);
        				 }
        			 }
        		 }
        		 return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        	 }
         });
		consumer.start();
		getSingleMQConsumer().put(key, consumer);
	}

	
	/**
	 * 初始化通知商户消费者MQ
	 * @throws MQClientException
	 */
	public void initHttpNotifyOrgConsumer() throws MQClientException {
		String key = consumerEnum.HttpNotifyOrgC.name();
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("httpNotifyOrgGroup");
		consumer.setNamesrvAddr(rocketMQNameSrvAddr);
		consumer.setInstanceName(key+localIp);
		consumer.setMessageModel(MessageModel.CLUSTERING);
		consumer.setVipChannelEnabled(false);
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.subscribe(MessageConstant.MQ_Topic_NotifyOrg+"_HTTP_NEW", MessageConstant.MQ_TAG_NotifyOrg+"_HTTP_NEW");
		consumer.registerMessageListener(new MessageListenerConcurrently() {
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
				if (list != null) {
					for (MessageExt ext : list) {
						String body = null;
						try {
							body = new String(ext.getBody(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							log.error("通知商户消费者获取消息数据转换错误:"+ext.getMsgId(), e);
							continue;
						}
						try {
							MqPushHttpBody notifyOrgBody = JSON.parseObject(body,MqPushHttpBody.class);
							String checkData = MD5Util.encrypt(notifyOrgBody.getRand(), MessageConstant.MQ_MD5_Key);
							if(!checkData.equals(notifyOrgBody.getCheckData())) {
								log.error("数据校验失败:"+body);
								continue;
							}
							Map<String, Object> params = notifyOrgBody.getParams();
							Set<Entry<String, Object>> entrySet = params.entrySet();
							LinkedMultiValueMap<Object, Object> map = new LinkedMultiValueMap<>();
							for (Entry<String, Object> entry : entrySet) {
								map.add(entry.getKey(),entry.getValue());
							}
							//将上游的回执结果，封装成我方协定的格式，通过HTTP协议，通知到下游商户
							HttpUtil.sendPostRequest(notifyOrgBody.getNotifyUrl(), map);
						} catch (Throwable e) {
							log.error("通知商户消费者获取消息处理失败:"+body, e);
						}
					}
				}
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}
		});
		consumer.start();
		getSingleMQConsumer().put(key, consumer);
	}
	
	 public SendResult sendBox(SimpleNote simpleNote) throws Exception {
        simpleNote.setCheckData(MD5Util.encrypt(simpleNote.getId().toString(), MessageConstant.MQ_MD5_Key));
        Message msg = new Message(MessageConstant.MQ_Topic_SendBox, MessageConstant.MQ_TAG_SendBox,
                JSON.toJSONString(simpleNote).getBytes(RemotingHelper.DEFAULT_CHARSET));
        //1-2 使用发件箱生产者SendBoxP 推送消息MQ_Topic_SendBox到MQ
        MQProducer producer = getProducer(ProducerEnum.SendBoxP.name());
        return producer.send(msg);
    }

	public SendResult sendRecord(String protocolType,String channelId,Long id) throws Exception {
		return sendRecord(false, protocolType, channelId, id);
	}

	public SendResult sendRecord(boolean isRetry,String protocolType,String channelId,Long id) throws Exception {
		SimpleNote simpleNote = new SimpleNote();
		simpleNote.setId(id);
		simpleNote.setCheckData(MD5Util.encrypt(id.toString(), MessageConstant.MQ_MD5_Key));
		String topic = null;
		if(isRetry) {
			topic = MessageConstant.MQ_Topic_SendRecord+"_Retry_" + protocolType;
		}else {
			topic = MessageConstant.MQ_Topic_SendRecord+ protocolType;
		}
		String tags = MessageConstant.MQ_TAG_SendRecord;
		if(!protocolType.equals(SmsChannelProtocolTypeEnums.Http.getCode())) {
			topic += channelId;
		}
		Message msg = new Message(topic, tags,JSON.toJSONString(simpleNote).getBytes(RemotingHelper.DEFAULT_CHARSET));
		msg.setKeys(id.toString());
		MQProducer producer = getProducer(ProducerEnum.SendBoxP.name());
		//2-2-4-1-1  使用发件箱生产者SendBoxP 将分拣后的信息 推送消息MQ_Topic_SendRecord**到MQ
		return producer.send(msg);
	}

	public SendResult upstreamReceipt(String data) throws Exception {
		SimpleNote simpleNote = new SimpleNote();
		simpleNote.setCheckData(MD5Util.encrypt(data, MessageConstant.MQ_MD5_Key));
		simpleNote.setData(data);
		String topic = MessageConstant.MQ_Topic_UpstreamReceipt;
		String tags = MessageConstant.MQ_TAG_UpstreamReceipt;
		Message msg = new Message(topic, tags,JSON.toJSONString(simpleNote).getBytes(RemotingHelper.DEFAULT_CHARSET));
		MQProducer producer = getProducer(ProducerEnum.UpstreamReceiptP.name());
		//3-1-3-1-1    使用发件箱回执处理生产者UpstreamReceiptP 将上游回执处理后的信息 推送消息MQ_Topic_UpstreamReceipt到MQ
		return producer.send(msg);
	}

	public SendResult httpNotify(MqPushHttpBody notifyOrgBody) throws Exception {
		notifyOrgBody.setCheckData(MD5Util.encrypt(notifyOrgBody.getRand(), MessageConstant.MQ_MD5_Key));
		Message msg = new Message(MessageConstant.MQ_Topic_NotifyOrg+"_HTTP_NEW", MessageConstant.MQ_TAG_NotifyOrg+"_HTTP_NEW",
						JSON.toJSONString(notifyOrgBody).getBytes(RemotingHelper.DEFAULT_CHARSET));
		MQProducer producer = getProducer(ProducerEnum.NotifyOrgP.name());
		//4-1-1-1-1-1 使用通知商户生产者 NotifyOrgP 将封装好的通知结果的信息 推送消息MQ_Topic_NotifyOrg+"_HTTP_NEW"到MQ
		return producer.send(msg);
	}
	
	public SendResult gateNotify(Integer type,NotifyMsgStateModel model) throws Exception {
		Message msg = new Message(MessageConstant.MQ_Topic_NotifyOrg+model.getOrgCode()+type, MessageConstant.MQ_TAG_NotifyOrg+model.getOrgCode()+type,
				JSON.toJSONString(model).getBytes(RemotingHelper.DEFAULT_CHARSET));
		MQProducer producer = getProducer(ProducerEnum.NotifyOrgP.name());
		//4-1-1-1-1-2 使用通知商户生产者 NotifyOrgP 将封装好的通知结果的信息 推送消息MQ_Topic_NotifyOrg+model.getOrgCode()+type到MQ
		return producer.send(msg);
	}
}
