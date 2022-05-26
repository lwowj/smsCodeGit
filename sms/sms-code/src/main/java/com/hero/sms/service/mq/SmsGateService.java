package com.hero.sms.service.mq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.MQPullConsumerScheduleService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.util.concurrent.RateLimiter;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.NotifyMsgStateModel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.channel.SmsChannelProtocolTypeEnums;
import com.hero.sms.enums.channel.SmsChannelPushPropertyNameEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.organization.OrgInterfaceTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.enums.organization.OrganizationPropertyNameEnums;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.impl.channel.push.SMPPClientMessageReceiveHandler;
import com.hero.sms.service.impl.channel.push.SMPPServerMessageReceiveHandler;
import com.hero.sms.service.mq.MQService.consumerEnum;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.utils.StringUtil;
import com.zx.sms.BaseMessage;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SmsGateService {
	
	private static Map<String, RateLimiter> rateLimiterMap;
	public static String smppServerName = "smppServer";

	@Autowired
    private ISmsChannelService smsChannelService;
    @Autowired
    private MQService mqService;
    @Autowired
    private IOrganizationService organizationService;
    
    private static final EndpointManager manager = EndpointManager.INS;
	
	public void initGateClient() throws MQClientException{
		rateLimiterMap = new ConcurrentHashMap<>();
		SmsChannel query = new SmsChannel();
		query.setProtocolType(SmsChannelProtocolTypeEnums.Smpp.getCode());
		query.setStateWith(ChannelStateEnums.START.getCode()+ChannelStateEnums.Pause.getCode());
		List<SmsChannelExt> channels = smsChannelService.findListContainProperty(query);
		if(CollectionUtils.isNotEmpty(channels)) {
			for (SmsChannelExt smsChannel : channels) {
				//3-1-1 初始化各个上游的smpp协议
				initGateClient(smsChannel);
			}
		}
	}
	
	public void initGateClient(SmsChannelExt smsChannel) throws MQClientException{
		if(rateLimiterMap == null) {
			rateLimiterMap = new ConcurrentHashMap<>();
		}
		String host = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.Host.getCode()).getValue();
		String port = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.Port.getCode()).getValue();
		String systemId = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.SystemId.getCode()).getValue();
		String password = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.Password.getCode()).getValue();
		String maxChannels = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.MaxChannels.getCode()).getValue();
		String systemType = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.SystemType.getCode()).getValue();
		String version = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.Version.getCode()).getValue();
		String speed = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.Speed.getCode()).getValue();
		/**
		 * @modify 2021-04-24
		 * 
		 */
		SmsChannelProperty writeLimitProperty = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.WriteLimit.getCode());
		String writeLimit = "200";
		if(writeLimitProperty != null)
		{
			writeLimit = StringUtil.isEmpty(writeLimitProperty.getValue())?"200":writeLimitProperty.getValue();
		}
		SmsChannelProperty readLimitProperty = smsChannel.getProperty(SmsChannelPushPropertyNameEnums.ReadLimit.getCode());
		String readLimit = "200";
		if(readLimitProperty != null)
		{
			readLimit = StringUtil.isEmpty(readLimitProperty.getValue())?"200":readLimitProperty.getValue();
		}
		
		/**
		 * @end
		 */
		double speedD = 20.0;
		if(StringUtils.isNotBlank(speed)) speedD = Double.parseDouble(speed);
		String id = smsChannel.getId().toString();
		rateLimiterMap.put(id, RateLimiter.create(speedD));
		SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
//		client.setIdleTimeSec((short)15);
		client.setId(id);
		client.setHost(host);
		client.setPort(Integer.parseInt(port));
		client.setSystemId(systemId);
		client.setPassword(password);
		if(StringUtils.isNotBlank(systemType)) client.setSystemType(systemType);
//		Address address = new Address();
//		address.setTon((byte)0);
//		client.setAddressRange(address);
		client.setChannelType(ChannelType.DUPLEX);
		client.setMaxChannels(Short.parseShort(maxChannels));
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
		client.setWriteLimit(Integer.parseInt(writeLimit+""));
		client.setReadLimit(Integer.parseInt(readLimit+""));
//		client.setWriteLimit(200);
//		client.setReadLimit(200);
		client.setInterfaceVersion(Byte.valueOf(version));
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		//3-1-3  在smpp请求参数中添加了广播SMPPClientMessageReceiveHandler（主要用于请求上游smpp接口后，上游将短信发送结果以smpp协议形式推送到我方时，进行参数处理）
		clienthandlers.add( new SMPPClientMessageReceiveHandler()); 
		client.setBusinessHandlerSet(clienthandlers);
		
		manager.addEndpointEntity(client);
		for (int i = 0; i < client.getMaxChannels(); i++) {
            manager.openEndpoint(client);
		}
		//3-1-2 初始化各个上游通道的分拣结果处理消费者
		mqService.initSendRecordConsumer(smsChannel);
	}
	
	public void closeClient(String id) throws Exception{
		String key = consumerEnum.SendRecordSMSGATEC.name()+id;
		MQPullConsumerScheduleService pullConsumerSchedule = MQService.getPullConsumerSchedule(key);
		if(pullConsumerSchedule != null) {
			pullConsumerSchedule.shutdown();
			MQService.getSingleMQPullConsumerSchedule().remove(key);
		}
		Thread.sleep(10000);
		manager.remove(id);
	}
	
	public SMPPServerEndpointEntity getSmppServer() {
		return getSmppServer(true);
	}
	public SMPPServerEndpointEntity getSmppServer(boolean isCreate) {
		SMPPServerEndpointEntity server = (SMPPServerEndpointEntity)manager.getEndpointEntity(smppServerName);
		if(isCreate && server == null) {
			Code hostCode = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig", "host");
			Code portCode = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig", "port");
			server = new SMPPServerEndpointEntity();
			server.setId(smppServerName);
			server.setHost(hostCode.getName());
			server.setPort(Integer.parseInt(portCode.getName()));
			server.setValid(true);
			//使用ssl加密数据流
			server.setUseSSL(false);
			manager.addEndpointEntity(server);
			manager.openEndpoint(server);
		}
		return server;
	}
	
	public void initGateServer() throws MQClientException {
		Organization organization = new Organization();
		organization.setStatus(OrgStatusEnums.Normal.getCode());
		organization.setInterfaceType(OrgInterfaceTypeEnums.Smpp.getCode());
		List<OrganizationExt> organizations = organizationService.findListContainProperty(organization);
		if(CollectionUtils.isEmpty(organizations)) {
			return;
		}
		SMPPServerEndpointEntity server = getSmppServer();
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		//6-1-1  在smpp请求参数中添加了广播SMPPServerMessageReceiveHandler（主要用于提供给下游请求短信smpp接口，下游请求发送短信时，将接受到的信息进行校验和逻辑处理，最后提交到上游）
		serverhandlers.add(new SMPPServerMessageReceiveHandler());
		for (OrganizationExt organizationExt : organizations) {
			List<OrganizationProperty> propertys = organizationExt.getPropertyByPropertyType(OrgInterfaceTypeEnums.Smpp.getCode());
			if(CollectionUtils.isEmpty(propertys)) {
				continue;
			}
			SMPPServerChildEndpointEntity child = new SMPPServerChildEndpointEntity();
			child.setId(organizationExt.getOrganizationCode());
			child.setSystemId(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.SystemId.getCode()).getValue());
			child.setPassword(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.Password.getCode()).getValue());
			child.setValid(true);
			child.setChannelType(ChannelType.DUPLEX);
			int maxChannels = Integer.parseInt(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.MaxChannels.getCode()).getValue());
			child.setMaxChannels((short)maxChannels);
			child.setRetryWaitTimeSec((short)30);
			child.setMaxRetryCnt((short)3);
			child.setReSendFailMsg(true);
			child.setIdleTimeSec((short)15);
			child.setWriteLimit(Integer.parseInt(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.ReadLimit.getCode()).getValue()));
			child.setReadLimit(Integer.parseInt(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.ReadLimit.getCode()).getValue()));
			child.setBusinessHandlerSet(serverhandlers);
			server.addchild(child);
			//6-1-2  初始化通知下游商户的消费者
			mqService.initGateNotifyConsumer(OrgInterfaceTypeEnums.Smpp.getCode(), organizationExt);
		}
	}
	
	public void initGateServerChild(OrganizationExt organizationExt) throws MQClientException {
		List<OrganizationProperty> propertys = organizationExt.getPropertyByPropertyType(OrgInterfaceTypeEnums.Smpp.getCode());
		if(CollectionUtils.isEmpty(propertys)) {
			return;
		}
		SMPPServerEndpointEntity server = getSmppServer();
		
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SMPPServerMessageReceiveHandler());
		
		SMPPServerChildEndpointEntity child = (SMPPServerChildEndpointEntity)server.getChild(organizationExt.getOrganizationCode(), ChannelType.DUPLEX);
		if(child == null) {
			child = new SMPPServerChildEndpointEntity();
			child.setId(organizationExt.getOrganizationCode());
			child.setSystemId(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.SystemId.getCode()).getValue());
			child.setPassword(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.Password.getCode()).getValue());
			child.setValid(true);
			child.setChannelType(ChannelType.DUPLEX);
			child.setRetryWaitTimeSec((short)30);
			child.setMaxRetryCnt((short)3);
			child.setReSendFailMsg(true);
			child.setIdleTimeSec((short)15);
			child.setBusinessHandlerSet(serverhandlers);
		}
		int maxChannels = Integer.parseInt(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.MaxChannels.getCode()).getValue());
		child.setMaxChannels((short)maxChannels);
		child.setWriteLimit(Integer.parseInt(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.ReadLimit.getCode()).getValue()));
		child.setReadLimit(Integer.parseInt(organizationExt.getProperty(OrgInterfaceTypeEnums.Smpp.getCode(), OrganizationPropertyNameEnums.ReadLimit.getCode()).getValue()));
		server.addchild(child);
		mqService.initGateNotifyConsumer(OrgInterfaceTypeEnums.Smpp.getCode(), organizationExt);
	}
	
	public void deleteGateServerChild(OrganizationExt organizationExt) throws InterruptedException {
		SMPPServerEndpointEntity server = getSmppServer(false);
		if(server == null) {
			return;
		}
		String key = consumerEnum.GateNotifyOrgC.name()+organizationExt.getOrganizationCode()+OrgInterfaceTypeEnums.Smpp.getCode();
		MQPullConsumerScheduleService consumer = MQService.getPullConsumerSchedule(key);
		if(consumer != null) {
			consumer.shutdown();
			MQService.getSingleMQPullConsumerSchedule().remove(key);
		}
		Thread.sleep(10000);
		EndpointEntity child = server.getChild(organizationExt.getOrganizationCode(), ChannelType.DUPLEX);
		if(child == null) return;
		server.removechild(child);
		manager.close(child);
	}
	
	public static void notify(EndpointEntity entity,NotifyMsgStateModel model) throws Exception{
			DeliverSmReceipt report = new DeliverSmReceipt();
			report.setId(model.getSendCode());
			if(StringUtils.isNotBlank(model.getSourceNumber())) {
				report.setDestAddress(new Address((byte)0,(byte)0,model.getSourceNumber()));
			}
			report.setSourceAddress(new Address((byte)0,(byte)0,model.getMobile()));
			if(model.getState().equals(String.valueOf(SendRecordStateEnums.ReceiptSuccess.getCode()))) {
				report.setStat("DELIVRD");
			}else if(model.getState().equals(String.valueOf(SendRecordStateEnums.ReceiptFail.getCode()))) {
				report.setStat("UNDELIVERABLE");
			}else if(model.getState().equals(String.valueOf(SendRecordStateEnums.SortingFail.getCode()))) {
				report.setStat("SortingFail");
			}else {
				return;
			}
			report.setText(model.getMsg());
			report.setSubmit_date(DateUtil.getString(new Date(), "yyyyMMddHHmm"));
			if(model.getReceiptTime() != null) {
				report.setDone_date(DateUtil.getString(model.getReceiptTime(), "yyyyMMddHHmm"));
			}else {
				report.setDone_date(report.getSubmit_date());
			}
			//6-1-2-1-1 smpp请求下游的工具方法
			List<Promise<BaseMessage>> results = ChannelUtil.syncWriteLongMsgToEntity(entity, report);
			/*if(CollectionUtils.isNotEmpty(results)) {
				for (Promise<BaseMessage> promise : results) {
					promise.sync();
					BaseMessage baseMessage = promise.get();
					log.debug(String.format("[%s][%s]回执响应信息【%s】", model.getSendCode(),model.getMobile(),JSON.toJSONString(baseMessage)));
				}
			}*/
	}
	
	public static List<Promise<BaseMessage>> sendSms(SendRecord sendRecord) throws Exception{
		return sendSms(1, sendRecord);
	}

	private static List<Promise<BaseMessage>> sendSms(int retryNum,SendRecord sendRecord) throws Exception{
 	    String beginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 	    
		String id = sendRecord.getChannelId().toString();
		RateLimiter rateLimiter = rateLimiterMap.get(id);
		rateLimiter.acquire();
		SubmitSm submitSm = new SubmitSm();
		//2022-03-22 进行编码转换
		String  smsNumberArea = DatabaseCache.getAreaCodeOutArea(sendRecord.getSmsNumberArea());
		String destAddress = smsNumberArea+sendRecord.getSmsNumber();
		destAddress = StringUtils.removeStart(destAddress, "+");
		if(StringUtils.isNotBlank(sendRecord.getSourceNumber())) submitSm.setSourceAddress(new Address((byte)0,(byte)0,sendRecord.getSourceNumber()));
		submitSm.setDestAddress(new Address((byte)0,(byte)0,destAddress));
		submitSm.setSmsMsg(sendRecord.getSmsContent());
		//3-1-2-2-2-1 smpp请求上游的工具方法
		List<Promise<BaseMessage>> result = ChannelUtil.syncWriteLongMsgToEntity(sendRecord.getChannelId().toString(), submitSm);
		//3-1-2-2-2-2 校验请求是否有响应，若没响应，重新推送，上限3次
		if(result == null) {
			String endTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			long runtime = DateUtil.getTime(beginTime, endTime,DateUtil.Y_M_D_H_M_S_S_2);
			//2021-02-02 
			log.info(String.format("【5、smpp推送消息未响应】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】", sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId()),beginTime,endTime,String.valueOf(runtime)));
//			log.info(String.format("消息推送未响应【%s】【%s】需重推", sendRecord.getSendCode(),sendRecord.getSmsNumber()));
			String sendSmsAgainSwitch = "OFF";
	 		Code sendSmsAgainSwitchSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsAgainSwitch");
	 	    if(sendSmsAgainSwitchSwitchCode!=null&&!"".equals(sendSmsAgainSwitchSwitchCode.getName()))
	 	    {
	 	    	sendSmsAgainSwitch = sendSmsAgainSwitchSwitchCode.getName();
	 	    }
	 	    if("ON".equals(sendSmsAgainSwitch))
	 	    {
	 	    	int sendSmsAgainRetryNum = 1;
		 		Code sendSmsAgainRetryNumCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsAgainRetryNum");
		 	    if(sendSmsAgainRetryNumCode!=null&&!"".equals(sendSmsAgainRetryNumCode.getName()))
		 	    {
		 	    	String sendSmsAgainRetryNumStr = sendSmsAgainRetryNumCode.getName();
		 	    	try {
		 	    		sendSmsAgainRetryNum = Integer.valueOf(sendSmsAgainRetryNumStr);
					} catch (Exception e) {
						sendSmsAgainRetryNum = 0;
					}
		 	    }
				if(retryNum >= sendSmsAgainRetryNum) 
				{
					log.info(String.format("【5-2、消息重推超过%s次】【%s】【%s】【%s】", String.valueOf(sendSmsAgainRetryNum),sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId())));
					return null;
				}
				
				int sendSmsAgainSleepNum = 1000;
		 		Code sendSmsAgainSleepNumCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsAgainSleepNum");
		 	    if(sendSmsAgainSleepNumCode!=null&&!"".equals(sendSmsAgainSleepNumCode.getName()))
		 	    {
		 	    	String sendSmsAgainSleepNumStr = sendSmsAgainSleepNumCode.getName();
		 	    	try {
		 	    		sendSmsAgainSleepNum = Integer.valueOf(sendSmsAgainSleepNumStr);
					} catch (Exception e) {
						sendSmsAgainSleepNum = 1000;
					}
		 	    }
				
				Thread.sleep(sendSmsAgainSleepNum*retryNum);
				retryNum++;
				log.info(String.format("【5-1、smpp推送消息未响应,第%s次进行重推】【%s】【%s】【%s】", String.valueOf(retryNum),sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId())));
				return sendSms(retryNum,sendRecord);
	 	    }
		}
		return result;
	}
	
	/**
	 * 异步提交方式
	 * @param sendRecord
	 * @return
	 * @throws Exception
	 */
	public static ChannelFuture sendSmsAsyn(SendRecord sendRecord) throws Exception{
		return sendSmsAsyn(1, sendRecord);
	}

	private static ChannelFuture sendSmsAsyn(int retryNum,SendRecord sendRecord) throws Exception{
 	    String beginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 	    
		String id = sendRecord.getChannelId().toString();
		RateLimiter rateLimiter = rateLimiterMap.get(id);
		rateLimiter.acquire();
		SubmitSm submitSm = new SubmitSm();
		//2022-03-22 进行编码转换
		String  smsNumberArea = DatabaseCache.getAreaCodeOutArea(sendRecord.getSmsNumberArea());
		String destAddress = smsNumberArea+sendRecord.getSmsNumber();
		destAddress = StringUtils.removeStart(destAddress, "+");
		submitSm.setSourceAddress(new Address((byte)0,(byte)0,String.valueOf(sendRecord.getId())));
		submitSm.setDestAddress(new Address((byte)0,(byte)0,destAddress));
		submitSm.setSmsMsg(sendRecord.getSmsContent());
		//3-1-2-2-2-1 smpp请求上游的工具方法
		ChannelFuture result = ChannelUtil.asyncWriteToEntity(sendRecord.getChannelId().toString(), submitSm);
		//3-1-2-2-2-2 校验请求是否有响应，若没响应，重新推送，上限3次
		if(result == null) {
			String endTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			long runtime = DateUtil.getTime(beginTime, endTime,DateUtil.Y_M_D_H_M_S_S_2);
			//2021-02-02 
			log.info(String.format("【5、smpp(异步)推送消息未响应】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】", sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId()),beginTime,endTime,String.valueOf(runtime)));
//			log.info(String.format("消息推送未响应【%s】【%s】需重推", sendRecord.getSendCode(),sendRecord.getSmsNumber()));
			String sendSmsAgainSwitch = "OFF";
	 		Code sendSmsAgainSwitchSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsAgainSwitch");
	 	    if(sendSmsAgainSwitchSwitchCode!=null&&!"".equals(sendSmsAgainSwitchSwitchCode.getName()))
	 	    {
	 	    	sendSmsAgainSwitch = sendSmsAgainSwitchSwitchCode.getName();
	 	    }
	 	    if("ON".equals(sendSmsAgainSwitch))
	 	    {
	 	    	int sendSmsAgainRetryNum = 1;
		 		Code sendSmsAgainRetryNumCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsAgainRetryNum");
		 	    if(sendSmsAgainRetryNumCode!=null&&!"".equals(sendSmsAgainRetryNumCode.getName()))
		 	    {
		 	    	String sendSmsAgainRetryNumStr = sendSmsAgainRetryNumCode.getName();
		 	    	try {
		 	    		sendSmsAgainRetryNum = Integer.valueOf(sendSmsAgainRetryNumStr);
					} catch (Exception e) {
						sendSmsAgainRetryNum = 0;
					}
		 	    }
				if(retryNum >= sendSmsAgainRetryNum) 
				{
					log.info(String.format("【5-2、(异步)消息重推超过%s次】【%s】【%s】【%s】", String.valueOf(sendSmsAgainRetryNum),sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId())));
					return null;
				}
				
				int sendSmsAgainSleepNum = 1000;
		 		Code sendSmsAgainSleepNumCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsAgainSleepNum");
		 	    if(sendSmsAgainSleepNumCode!=null&&!"".equals(sendSmsAgainSleepNumCode.getName()))
		 	    {
		 	    	String sendSmsAgainSleepNumStr = sendSmsAgainSleepNumCode.getName();
		 	    	try {
		 	    		sendSmsAgainSleepNum = Integer.valueOf(sendSmsAgainSleepNumStr);
					} catch (Exception e) {
						sendSmsAgainSleepNum = 1000;
					}
		 	    }
				
				Thread.sleep(sendSmsAgainSleepNum*retryNum);
				retryNum++;
				log.info(String.format("【5-1、smpp(异步)推送消息未响应,第%s次进行重推】【%s】【%s】【%s】", String.valueOf(retryNum),sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId())));
				return sendSmsAsyn(retryNum,sendRecord);
	 	    }
		}
		return result;
	}
}
