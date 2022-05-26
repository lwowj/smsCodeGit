package com.hero.sms.service.impl.channel.push;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoPlan;
import com.hero.sms.enums.channel.SmsChannelProtocolTypeEnums;
import com.hero.sms.enums.channel.SmsChannelPushPropertyNameEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.mapper.message.SendRecordCheckinfoMapper;
import com.hero.sms.mapper.message.SendRecordCheckinfoPlanMapper;
import com.hero.sms.mapper.message.SendRecordMapper;
import com.hero.sms.service.channel.push.IBaseSmsPushService;
import com.hero.sms.service.mq.MQService;
import com.hero.sms.service.mq.SmsGateService;
import com.hero.sms.utils.StringUtil;
import com.zx.sms.BaseMessage;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息推送基础类
 * @author Lenovo
 *
 */
@Slf4j
public class BaseSmsPushService implements IBaseSmsPushService{

	protected String reqUrl; // 请求地址
	protected int connectTimeout = 5000;// 请求超时时间
	protected int readTimeout = 5000;// 读取超时时间
	protected String orgNo; // 商户编号
	protected String account; // 账号
	protected String signKey; // 签名秘钥
	protected String charset; // 编码
	
	private SmsChannelExt smsChannelext;
	protected int channelId; // 通道ID
	protected String channelCode; // 通道code
	protected String queryUrl; // 查询地址
	protected String requestMethod;//请求方式 GET POST
	
	
	public void init(SmsChannelExt smsChannelext) {
		this.smsChannelext = smsChannelext;
		channelId = smsChannelext.getId();
		channelCode = smsChannelext.getCode();
		reqUrl = property(SmsChannelPushPropertyNameEnums.ReqUrl.getCode());
		orgNo = property(SmsChannelPushPropertyNameEnums.OrgNo.getCode());
		account = property(SmsChannelPushPropertyNameEnums.Account.getCode());
		signKey = property(SmsChannelPushPropertyNameEnums.SignKey.getCode());
		String connectTimeoutP = property(SmsChannelPushPropertyNameEnums.ConnectTimeout.getCode());
		if(StringUtils.isNotBlank(connectTimeoutP)) {
			connectTimeout = Integer.valueOf(connectTimeoutP);
		}
		String readTimeoutP = property(SmsChannelPushPropertyNameEnums.ReadTimeout.getCode());
		if(StringUtils.isNotBlank(readTimeoutP)) {
			readTimeout = Integer.valueOf(readTimeoutP);
		}
		queryUrl = property(SmsChannelPushPropertyNameEnums.QueryUrl.getCode());
		charset = property(SmsChannelPushPropertyNameEnums.Charset.getCode());
		if(StringUtils.isBlank(charset)) {
			charset = "UTF-8";
		}
		requestMethod = property(SmsChannelPushPropertyNameEnums.RequestMethod.getCode());
		if(StringUtils.isBlank(requestMethod)) {
			requestMethod = "POST";
		}
	}
	
	public boolean push(SendRecord sendRecord) {
		String protocolType = smsChannelext.getProtocolType();
		if(protocolType.equals(SmsChannelProtocolTypeEnums.Http.getCode())) {
			return httpPush(sendRecord);
		}
		if(protocolType.equals(SmsChannelProtocolTypeEnums.Smpp.getCode())) {
			return smppPush(sendRecord);
		}
		return false;
	}
	
	@Override
	public boolean httpPush(SendRecord sendRecord) {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean smppPush(SendRecord sendRecord) {
		try {
	    	log.info(String.format("【5-0、SMPP(同步)提交】【%s】【%s】【%s】", sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId())));
	    	//3-1-2-2-2 调用上游smpp接口进行短信发送 
			List<Promise<BaseMessage>> results = SmsGateService.sendSms(sendRecord);
			if(CollectionUtils.isNotEmpty(results)) {
				for (Promise<BaseMessage> promise : results) {
					//3-1-2-2-3 处理请求上游后的响应结果 
					promise.addListener(new MyFutureListener(sendRecord));
				}
			}
			else
			{
		        /**
		         * @begin 2021-02-03 
		         * 【请求返回为空时，进行相应逻辑处理，是否重新推送发送】
		         * 异常原因：当请求超时或网络堵塞时，会返回空值；
		         * 1、网关类型判断：确认发件箱生产者SendBoxP是否初始化；未初始化则无法推送MQ，直接跳出处理
		         * 2、发送记录唯一约束判断：确认校验开关是否开启；开启则进行解锁成功后，推送；关闭则入库记录数据，防止二次异常后重复推送，限制仅推送一次
		         */
				boolean needUpStateFlag = true;
				Integer gatewayType = null;
				try 
				{
					String gatewayTypeStr = DatabaseCache.getGatewayType();
					if(StringUtil.isNotBlank(gatewayTypeStr))
					{
						gatewayType = Integer.valueOf(gatewayTypeStr);
					}
				} catch (Exception e) {}
				/**
		         * 网关类型判断
		         * 如果取值为1，则重新推送到MQ中；不为1，则修改记录状态与描述
		         * 逻辑原由：当gatewayType只配置为4时，未初始化MQ生产者SendBoxP，若推送到MQ会报空指针异常（即无法获取到SendBoxP）；
		         * 故做此逻辑判断，若有初始化SendBoxP，则重新加入MQ推送；若单独部署未初始化SendBoxP，则请求超时失败后，做记录状态修改
		         * 
		         */
		        if(gatewayType != null && (gatewayType&1) == 1) 
		        {
		        	/**
		    		 * 判断中间唯一校验是否开启
		    		 * 若开启，则重新推送也会被拦截，重新推送为无意义操作
		    		 * 故判断为开启，不推送；关闭则进行重新推送
		    		 * 
		    		 */
		    		String sendRecordCheckinfoSwitch = "ON";
		     		Code sendRecordCheckinfoSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckinfoSwitch");
		     	    if(sendRecordCheckinfoSwitchCode!=null&&!"".equals(sendRecordCheckinfoSwitchCode.getName()))
		     	    {
		     	    	sendRecordCheckinfoSwitch = sendRecordCheckinfoSwitchCode.getName();
		     	    }
		     	    
		     	    boolean upSendRecordCheckinfoFlag = false;
		     	   /**
		     	    * 校验发送记录唯一约束开关逻辑判断
		     	    * 唯一约束开启：表示发送记录已有数据,进行更新记录状态（解锁）动作;修改成功则表示该记录还未重发过，修改失败则表示已经重发过，无法继续重发
		     	    * 唯一约束关闭：表示未校验记录是否已重推；则利用中间表入库一条数据，确保记录只能重发一次，第二次则报唯一约束错误，需人工处理
	     	    	*/
		     	    if(!"OFF".equals(sendRecordCheckinfoSwitch))
		     	    {
		     	    	String sendRecordCheckInfoWay = "0";
		        		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
		        	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
		        	    {
		        	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
		        	    }	
		        	    
		     	    	try 
		     	    	{
		     	    		int count = 0;
		     	    		 //1、通过记录id校验； 0或其他 通过批次号与号码校验
			 	    	    if("1".equals(sendRecordCheckInfoWay))
			 	    	    {
			 	    	    	SendRecordCheckinfoPlanMapper sendRecordCheckinfolanMapper = SpringContextUtil.getBean(SendRecordCheckinfoPlanMapper.class);
			 	    	    	LambdaUpdateWrapper<SendRecordCheckinfoPlan> updateSendRecordCheckinfoPlanWrapper = new LambdaUpdateWrapper<>();
			 	    	    	updateSendRecordCheckinfoPlanWrapper.eq(SendRecordCheckinfoPlan::getState,1);//锁定状态1
			 	    	    	updateSendRecordCheckinfoPlanWrapper.eq(SendRecordCheckinfoPlan::getSendRecordId,sendRecord.getId());
			 	        		updateSendRecordCheckinfoPlanWrapper.set(SendRecordCheckinfoPlan::getState,0);//修改为解锁状态0
			 	        		count = sendRecordCheckinfolanMapper.update(null,updateSendRecordCheckinfoPlanWrapper);
			 	    	    }
			 	    	    else
			 	    	    {
			 	    	    	SendRecordCheckinfoMapper sendRecordCheckinfoMapper = SpringContextUtil.getBean(SendRecordCheckinfoMapper.class);
			 	    	    	LambdaUpdateWrapper<SendRecordCheckinfo> updateSendRecordCheckinfoWrapper = new LambdaUpdateWrapper<>();
			 	        		updateSendRecordCheckinfoWrapper.eq(SendRecordCheckinfo::getState,1);//锁定状态1
			 	        		updateSendRecordCheckinfoWrapper.eq(SendRecordCheckinfo::getSmsNumber,sendRecord.getSmsNumber());
			 	        		updateSendRecordCheckinfoWrapper.eq(SendRecordCheckinfo::getSendCode,sendRecord.getSendCode());
			 	        		updateSendRecordCheckinfoWrapper.set(SendRecordCheckinfo::getState,0);//修改为解锁状态0
			     	    		count = sendRecordCheckinfoMapper.update(null,updateSendRecordCheckinfoWrapper);
			 	    	    }
			 	    	    
			 	    	    if(count>0)
		     	    		{
		     	    			needUpStateFlag = false;
			     	    		upSendRecordCheckinfoFlag = true;
		     	    		}
		     	    		else
		     	    		{
		     	    			needUpStateFlag = true;
			     	    		upSendRecordCheckinfoFlag = false;
		     	    		}
						} catch (Exception e) {
							needUpStateFlag = true;
		     	    		upSendRecordCheckinfoFlag = false;
						}
		     	    }
		     	    else
		     	    {
		     	    	try 
		     	    	{
		     	    		needUpStateFlag = false;
		     	    		upSendRecordCheckinfoFlag = true;
						} catch (Exception e) {
							needUpStateFlag = true;
		     	    		upSendRecordCheckinfoFlag = false;
						}
		     	    }
		     	    if(upSendRecordCheckinfoFlag)
		     	    {
		     	    	Thread.sleep(1000);
						MQService mqService = SpringContextUtil.getBean(MQService.class);
						mqService.sendRecord(true,SmsChannelProtocolTypeEnums.Smpp.getCode(), sendRecord.getChannelId().toString(), sendRecord.getId());
					    log.error(String.format("批次号【%s】手机号码【%s】RESEND", sendRecord.getSendCode(),sendRecord.getSmsNumber()));
		     	    }
		        }
		        if(needUpStateFlag)
		        {
					//2021-02-03 打开请求无响应修改状态，不做添加到发送队列。需要手工推送 
					LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
			        updateWrapper.set(SendRecord::getState, SendRecordStateEnums.ReqFail.getCode());
			        updateWrapper.set(SendRecord::getSubmitTime, new Date());
			        updateWrapper.set(SendRecord::getStateDesc, "网络堵塞");
			        updateWrapper.eq(SendRecord::getId, sendRecord.getId());
			        SendRecordMapper sendRecordMapper = SpringContextUtil.getBean(SendRecordMapper.class);
			        sendRecordMapper.update(null,updateWrapper);
		        }
		        /**
		         * @end
		         */
			}
		} catch (Exception e) {
			log.error(String.format("批次号【%s】手机号码【%s】发送smpp失败", sendRecord.getSendCode(),sendRecord.getSmsNumber()),e);
		}
		return false;
	}
	
	class MyFutureListener implements GenericFutureListener {
		
		private SendRecord sendRecord;
		
		public MyFutureListener(SendRecord sendRecord) {
			super();
			this.sendRecord = sendRecord;
		}

		@Override
		public void operationComplete(Future future) throws Exception {
			//2020-12-14 新增校验 发送记录状态为“提交失败”的记录 SendRecordStateEnums.ReqFail.getCode()
			if (sendRecord.getState().intValue() != SendRecordStateEnums.WaitReq.getCode()&&sendRecord.getState().intValue() != SendRecordStateEnums.ReqFail.getCode()) return;
			String getStateDesc = sendRecord.getStateDesc();
			//接收成功，如果失败可以获取失败原因，比如遇到连接突然中断错误等等
			if(future.isSuccess()){
				SubmitSmResp resp = (SubmitSmResp)future.get();
				String respJson = JSON.toJSONString(resp);
				//@begin 2022-04-08
				//判断上游通道是否返回 唯一标识ID
				if(StringUtil.isNotBlank(resp.getMessageId()))
				{
					sendRecord.setResMsgid(resp.getMessageId());
					sendRecord.setState(SendRecordStateEnums.ReqSuccess.getCode());
					log.info(String.format("【同步提交】批次号【%s】手机号码【%s】通道ID【%s】发送smpp【成功】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),respJson));
					getStateDesc = "";
				}
				else
				{
					sendRecord.setState(SendRecordStateEnums.ReqFail.getCode());
					
					//保存上游通道失败的消息内容 2022-04-08
					String resultMessage = resp.getResultMessage();
					if(StringUtil.isNotBlank(resultMessage))
					{
						if(resultMessage.length()>100) 
						{
							resultMessage = resultMessage.substring(0, 100);
						}
						sendRecord.setStateDesc(resultMessage);
						getStateDesc = resultMessage;
					}
					else
					{
						sendRecord.setStateDesc("未返回ID");
						getStateDesc = "未返回ID";
					}
					
					log.info(String.format("【同步提交】批次号【%s】手机号码【%s】通道ID【%s】发送smpp【请求成功，但未返回ID】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),respJson));
				}
			}else{
				sendRecord.setState(SendRecordStateEnums.ReqFail.getCode());
				log.error(String.format("【同步提交】批次号【%s】手机号码【%s】通道ID【%s】发送smpp[失败]返回失败状态",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId()));
			}
			
			String showRunTimeLogSwitch = "OFF";
	 		Code showRunTimeLogSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogSwitch");
	 	    if(showRunTimeLogSwitchCode!=null&&!"".equals(showRunTimeLogSwitchCode.getName()))
	 	    {
	 	    	showRunTimeLogSwitch = showRunTimeLogSwitchCode.getName();
	 	    }
			
			String beginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			
			LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
	        updateWrapper.set(SendRecord::getState, sendRecord.getState());
	        updateWrapper.set(SendRecord::getStateDesc, getStateDesc);
	        updateWrapper.set(SendRecord::getResMsgid, sendRecord.getResMsgid());
	        //2020-12-01 添加记录请求上游的时间
	        updateWrapper.set(SendRecord:: getSubmitTime, new Date());
	        updateWrapper.eq(SendRecord::getId, sendRecord.getId());
	        SendRecordMapper sendRecordMapper = SpringContextUtil.getBean(SendRecordMapper.class);
	        sendRecordMapper.update(null,updateWrapper);
	        
	        if("ON".equals(showRunTimeLogSwitch))
	 	    {
		        String endTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
				long runtime = DateUtil.getTime(beginTime, endTime,DateUtil.Y_M_D_H_M_S_S_2);
		        log.error(String.format("【6、发送smpp后修改状态耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),beginTime,endTime,String.valueOf(runtime)));
	 	    }
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public boolean smppPushAsyn(SendRecord sendRecord) 
	{
		try {
    	    log.info(String.format("【5-0、SMPP(异步)提交】【%s】【%s】【%s】", sendRecord.getSendCode(),sendRecord.getSmsNumber(),String.valueOf(sendRecord.getChannelId())));
	    	ChannelFuture results = SmsGateService.sendSmsAsyn(sendRecord);
			if(results != null) {
				//3-1-2-2-3 处理请求上游后的响应结果 
				results.addListener(new MyFutureAsynListener(sendRecord));
			}
			else
			{
		        /**
		         * @begin 2021-02-03 
		         * 【请求返回为空时，进行相应逻辑处理，是否重新推送发送】
		         * 异常原因：当请求超时或网络堵塞时，会返回空值；
		         * 1、网关类型判断：确认发件箱生产者SendBoxP是否初始化；未初始化则无法推送MQ，直接跳出处理
		         * 2、发送记录唯一约束判断：确认校验开关是否开启；开启则进行解锁成功后，推送；关闭则入库记录数据，防止二次异常后重复推送，限制仅推送一次
		         */
				boolean needUpStateFlag = true;
				Integer gatewayType = null;
				try 
				{
					String gatewayTypeStr = DatabaseCache.getGatewayType();
					if(StringUtil.isNotBlank(gatewayTypeStr))
					{
						gatewayType = Integer.valueOf(gatewayTypeStr);
					}
				} catch (Exception e) {}
				/**
		         * 网关类型判断
		         * 如果取值为1，则重新推送到MQ中；不为1，则修改记录状态与描述
		         * 逻辑原由：当gatewayType只配置为4时，未初始化MQ生产者SendBoxP，若推送到MQ会报空指针异常（即无法获取到SendBoxP）；
		         * 故做此逻辑判断，若有初始化SendBoxP，则重新加入MQ推送；若单独部署未初始化SendBoxP，则请求超时失败后，做记录状态修改
		         * 
		         */
		        if(gatewayType != null && (gatewayType&1) == 1) 
		        {
		        	/**
		    		 * 判断中间唯一校验是否开启
		    		 * 若开启，则重新推送也会被拦截，重新推送为无意义操作
		    		 * 故判断为开启，不推送；关闭则进行重新推送
		    		 * 
		    		 */
		    		String sendRecordCheckinfoSwitch = "ON";
		     		Code sendRecordCheckinfoSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckinfoSwitch");
		     	    if(sendRecordCheckinfoSwitchCode!=null&&!"".equals(sendRecordCheckinfoSwitchCode.getName()))
		     	    {
		     	    	sendRecordCheckinfoSwitch = sendRecordCheckinfoSwitchCode.getName();
		     	    }
		     	    
		     	    boolean upSendRecordCheckinfoFlag = false;
		     	   /**
		     	    * 校验发送记录唯一约束开关逻辑判断
		     	    * 唯一约束开启：表示发送记录已有数据,进行更新记录状态（解锁）动作;修改成功则表示该记录还未重发过，修改失败则表示已经重发过，无法继续重发
		     	    * 唯一约束关闭：表示未校验记录是否已重推；则利用中间表入库一条数据，确保记录只能重发一次，第二次则报唯一约束错误，需人工处理
	     	    	*/
		     	    if(!"OFF".equals(sendRecordCheckinfoSwitch))
		     	    {
		     	    	String sendRecordCheckInfoWay = "0";
		        		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
		        	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
		        	    {
		        	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
		        	    }	
		        	    
		     	    	try 
		     	    	{
		     	    		int count = 0;
		     	    		 //1、通过记录id校验； 0或其他 通过批次号与号码校验
			 	    	    if("1".equals(sendRecordCheckInfoWay))
			 	    	    {
			 	    	    	SendRecordCheckinfoPlanMapper sendRecordCheckinfolanMapper = SpringContextUtil.getBean(SendRecordCheckinfoPlanMapper.class);
			 	    	    	LambdaUpdateWrapper<SendRecordCheckinfoPlan> updateSendRecordCheckinfoPlanWrapper = new LambdaUpdateWrapper<>();
			 	    	    	updateSendRecordCheckinfoPlanWrapper.eq(SendRecordCheckinfoPlan::getState,1);//锁定状态1
			 	    	    	updateSendRecordCheckinfoPlanWrapper.eq(SendRecordCheckinfoPlan::getSendRecordId,sendRecord.getId());
			 	        		updateSendRecordCheckinfoPlanWrapper.set(SendRecordCheckinfoPlan::getState,0);//修改为解锁状态0
			 	        		count = sendRecordCheckinfolanMapper.update(null,updateSendRecordCheckinfoPlanWrapper);
			 	    	    }
			 	    	    else
			 	    	    {
			 	    	    	SendRecordCheckinfoMapper sendRecordCheckinfoMapper = SpringContextUtil.getBean(SendRecordCheckinfoMapper.class);
			 	    	    	LambdaUpdateWrapper<SendRecordCheckinfo> updateSendRecordCheckinfoWrapper = new LambdaUpdateWrapper<>();
			 	        		updateSendRecordCheckinfoWrapper.eq(SendRecordCheckinfo::getState,1);//锁定状态1
			 	        		updateSendRecordCheckinfoWrapper.eq(SendRecordCheckinfo::getSmsNumber,sendRecord.getSmsNumber());
			 	        		updateSendRecordCheckinfoWrapper.eq(SendRecordCheckinfo::getSendCode,sendRecord.getSendCode());
			 	        		updateSendRecordCheckinfoWrapper.set(SendRecordCheckinfo::getState,0);//修改为解锁状态0
			     	    		count = sendRecordCheckinfoMapper.update(null,updateSendRecordCheckinfoWrapper);
			 	    	    }
			 	    	    
			 	    	    if(count>0)
		     	    		{
		     	    			needUpStateFlag = false;
			     	    		upSendRecordCheckinfoFlag = true;
		     	    		}
		     	    		else
		     	    		{
		     	    			needUpStateFlag = true;
			     	    		upSendRecordCheckinfoFlag = false;
		     	    		}
						} catch (Exception e) {
							needUpStateFlag = true;
		     	    		upSendRecordCheckinfoFlag = false;
						}
		     	    }
		     	    else
		     	    {
		     	    	try 
		     	    	{
		     	    		needUpStateFlag = false;
		     	    		upSendRecordCheckinfoFlag = true;
						} catch (Exception e) {
							needUpStateFlag = true;
		     	    		upSendRecordCheckinfoFlag = false;
						}
		     	    }
		     	    if(upSendRecordCheckinfoFlag)
		     	    {
		     	    	Thread.sleep(1000);
						MQService mqService = SpringContextUtil.getBean(MQService.class);
						mqService.sendRecord(true,SmsChannelProtocolTypeEnums.Smpp.getCode(), sendRecord.getChannelId().toString(), sendRecord.getId());
					    log.error(String.format("批次号【%s】手机号码【%s】RESEND", sendRecord.getSendCode(),sendRecord.getSmsNumber()));
		     	    }
		        }
		        if(needUpStateFlag)
		        {
					//2021-02-03 打开请求无响应修改状态，不做添加到发送队列。需要手工推送 
					LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
			        updateWrapper.set(SendRecord::getState, SendRecordStateEnums.ReqFail.getCode());
			        updateWrapper.set(SendRecord::getSubmitTime, new Date());
			        updateWrapper.set(SendRecord::getStateDesc, "网络堵塞");
			        updateWrapper.eq(SendRecord::getId, sendRecord.getId());
			        SendRecordMapper sendRecordMapper = SpringContextUtil.getBean(SendRecordMapper.class);
			        sendRecordMapper.update(null,updateWrapper);
		        }
		        /**
		         * @end
		         */
			}
		} catch (Exception e) {
			log.error(String.format("批次号【%s】手机号码【%s】发送smpp失败", sendRecord.getSendCode(),sendRecord.getSmsNumber()),e);
		}
		return false;
	}
	
	class MyFutureAsynListener implements GenericFutureListener {
		
		private SendRecord sendRecord;
		
		public MyFutureAsynListener(SendRecord sendRecord) {
			super();
			this.sendRecord = sendRecord;
		}

		@Override
		public void operationComplete(Future future) throws Exception {
			//2020-12-14 新增校验 发送记录状态为“提交失败”的记录 SendRecordStateEnums.ReqFail.getCode()
			if (sendRecord.getState().intValue() != SendRecordStateEnums.WaitReq.getCode()&&sendRecord.getState().intValue() != SendRecordStateEnums.ReqFail.getCode()) return;
			String getStateDesc = sendRecord.getStateDesc();
			//接收成功，如果失败可以获取失败原因，比如遇到连接突然中断错误等等
			if(future.isSuccess())
			{
				getStateDesc = "";
				sendRecord.setState(SendRecordStateEnums.ReqSuccess.getCode());
				log.info(String.format("【异步提交】批次号【%s】记录ID【%s】手机号码【%s】通道ID【%s】发送smpp【成功】",sendRecord.getSendCode(),sendRecord.getId(),sendRecord.getSmsNumber(),sendRecord.getChannelId()));
			}
			else
			{
				sendRecord.setState(SendRecordStateEnums.ReqFail.getCode());
				log.error(String.format("【异步提交】批次号【%s】记录ID【%s】手机号码【%s】通道ID【%s】【异步提交】发送smpp[失败]返回失败状态",sendRecord.getSendCode(),sendRecord.getId(),sendRecord.getSmsNumber(),sendRecord.getChannelId()));
			}
			
			String showRunTimeLogSwitch = "OFF";
	 		Code showRunTimeLogSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogSwitch");
	 	    if(showRunTimeLogSwitchCode!=null&&!"".equals(showRunTimeLogSwitchCode.getName()))
	 	    {
	 	    	showRunTimeLogSwitch = showRunTimeLogSwitchCode.getName();
	 	    }
			
			String beginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			
			LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
	        updateWrapper.set(SendRecord::getState, sendRecord.getState());
	        updateWrapper.set(SendRecord::getStateDesc, getStateDesc);
	        //2020-12-01 添加记录请求上游的时间
	        updateWrapper.set(SendRecord:: getSubmitTime, new Date());
	        updateWrapper.eq(SendRecord::getId, sendRecord.getId());
	        updateWrapper.eq(SendRecord::getChannelId, sendRecord.getChannelId());
	        updateWrapper.isNull(SendRecord::getResMsgid);
	        SendRecordMapper sendRecordMapper = SpringContextUtil.getBean(SendRecordMapper.class);
	        sendRecordMapper.update(null,updateWrapper);
	        
	        if("ON".equals(showRunTimeLogSwitch))
	 	    {
		        String endTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
				long runtime = DateUtil.getTime(beginTime, endTime,DateUtil.Y_M_D_H_M_S_S_2);
		        log.error(String.format("【6、发送smpp后修改状态耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),beginTime,endTime,String.valueOf(runtime)));
	 	    }
		}
	}
	
	public String property(String name) {
		List<SmsChannelProperty> propertys = smsChannelext.getPropertys();
		if(CollectionUtils.isNotEmpty(propertys)) {
			for (SmsChannelProperty smsChannelProperty : propertys) {
				if(smsChannelProperty.getName().equals(name)) {
					return smsChannelProperty.getValue();
				}
			}
		}
		return null;
	}

	@Override
	public List<ReturnRecord> receipt(String resultData) {
		return null;
	}
	
	@Override
	public String query(SendRecord sendRecord) {
		return null;
	}
	
}
