package com.hero.sms.service.impl.channel.push;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.channel.SmsChannelSubmitWayEnums;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.mapper.message.SendRecordMapper;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.mq.MQService;
import com.hero.sms.utils.StringUtil;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SMPPClientMessageReceiveHandler extends AbstractBusinessHandler {

	@Override
	public String name() {
		return "smppMsgReceiveHandler";
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt == SessionState.Connect) {
			log.info(String.format("通道【%s】连接成功",getEndpointEntity().getId()));
		}
		if (evt == SessionState.DisConnect) {
			log.error(String.format("通道【%s】连接失败",getEndpointEntity().getId()));
		}
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof DeliverSmReceipt) {
			try {
				log.info("【回执】收到[上游]的消息："+JSON.toJSONString(msg));
			} catch (Exception e) {
				log.info("【回执】NO1收到[上游]消息转换JSON失败");
				try {
					log.info("【回执】NO2收到[上游]的消息："+ msg);
				} catch (Exception e1) {
					log.info("【回执】NO3收到[上游]的消息输出失败");
				}
			}
			DeliverSmReceipt e = (DeliverSmReceipt) msg;
//			String receiptMsgId = e.getOptionalParameter(SmppConstants.TAG_RECEIPTED_MSG_ID).getValueAsString();
//			String msgState = e.getOptionalParameter(SmppConstants.TAG_MSG_STATE).getValueAsString();
			String receiptMsgId = e.getId();
			String msgState = e.getStat();
			String channelId = getEndpointEntity().getId();
			String address = e.getSourceAddress().getAddress();
			if(!address.startsWith("+")) {
				address = "+"+address;
			}
			try {
				log.info(String.format("【回执】解析[上游]的消息：通道ID【%s】通道标识【%s】手机号码【%s】发送状态【%s】", channelId,receiptMsgId,address,msgState));
			} catch (Exception e2) {}
			
			Address destAddress = e.getDestAddress();
			
			try {
				ReturnRecord returnRecord = new ReturnRecord();
				returnRecord.setChannelId(Integer.valueOf(channelId));
				returnRecord.setResMsgid(receiptMsgId);
				
				returnRecord.setSmsNumber(address);
				if(msgState.equals("DELIVRD")) {
					returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
				}else {
					returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
				}
				if(destAddress != null) returnRecord.setSourceNumber(destAddress.getAddress());
				returnRecord.setReturnDesc(msgState);
				/*
				 * @begin
				 * 2021-05-11
				 * 上游通道描述
				 */
				try 
				{
					String stateDesc = e.getText();
					if(StringUtil.isNotBlank(stateDesc)) returnRecord.setReqDesc(stateDesc);
				}
				catch (Exception e2) {}
				/*
				 * @end
				 */
				
				MQService mqService = SpringContextUtil.getBean(MQService.class);
				//3-1-3-1-1    使用发件箱回执处理生产者UpstreamReceiptP 将上游回执处理后的信息 推送消息MQ_Topic_UpstreamReceipt到MQ
				mqService.upstreamReceipt(JSON.toJSONString(returnRecord));
				
				//3-1-3-2  以下为smpp的响应方式（即接收到上游参数后，smpp对应给予结果响应）
				DeliverSmResp res =e.createResponse();
				res.setMessageId(String.valueOf(System.currentTimeMillis()));
				ctx.writeAndFlush(res);
			} catch (Exception e1) {
				log.error(String.format("回执处理失败，通道【%s】手机号码【%s】msgId【%s】",channelId,address,receiptMsgId),e1);
			}
		}
		else if (msg instanceof SubmitSmResp) 
		{
			String sendSmsWaySwitch = "OFF";
    		Code sendSmsWaySwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsWaySwitch");
    	    if(sendSmsWaySwitchCode!=null&&!"".equals(sendSmsWaySwitchCode.getName()))
    	    {
    	    	sendSmsWaySwitch = sendSmsWaySwitchCode.getName();
    	    }
    	    //当配置开关开启，则判断通道自身配置，是否为异步提交
    	    if("ON".equals(sendSmsWaySwitch))
    	    {
    			String channelId = getEndpointEntity().getId();
    			int channelIdInt = 0;
    			try {
    				channelIdInt = Integer.valueOf(channelId).intValue();
				} catch (Exception e) {
					// TODO: handle exception
				}
    			
    			SmsChannel smsChannel = null;
    			boolean querySmsChannelFlag = true;
    			List<SmsChannel> getSmsChannelList = DatabaseCache.getSmsChannelList();
     	    	if(getSmsChannelList != null && getSmsChannelList.size()>0)
     	    	{
     	    		smsChannel = DatabaseCache.getSmsChannelById(channelIdInt);
     	    		querySmsChannelFlag = false;
     	    	}
     	    	
     	    	if(querySmsChannelFlag)
     	 	    {
     	    		ISmsChannelService smsChannelService = SpringContextUtil.getBean(ISmsChannelService.class);
     	 	    	smsChannel = smsChannelService.getById(channelIdInt);
     	 	    }
    			
    			int submitWay = 0;
    	    	try {
    	    		submitWay = smsChannel.getSubmitWay();
				} catch (Exception e) {}
    	    	
    	    	//判断通道配置是否是 异步提交方式
    	    	if(submitWay == SmsChannelSubmitWayEnums.ASYN.getCode())
    	    	{
    	    		try {
    	    			log.info(String.format("【异步提交回执】收到[上游]通道ID【%s】的消息:%s",channelIdInt,JSON.toJSONString(msg)));
        			} catch (Exception e) {
        				log.info(String.format("【异步提交回执】NO1收到[上游]通道ID【%s】消息转换JSON失败",channelIdInt));
        				try {
        					log.info(String.format("【异步提交回执】NO2收到[上游]通道ID【%s】的消息:%s",channelIdInt,JSON.toJSONString(msg)));
        				} catch (Exception e1) {
        					log.info(String.format("【异步提交回执】NO3收到[上游]通道ID【%s】消息转换JSON失败",channelIdInt));
        				}
        			}
    	    		
    	    		try 
    	    		{
    	    			SubmitSmResp resp = (SubmitSmResp) msg;
            			String respJson = JSON.toJSONString(resp);
            			SubmitSm submitSm = (SubmitSm)resp.getRequest();
            			String resMsgid = resp.getMessageId();
            			String resultMessage = resp.getResultMessage();
            			boolean isResponse = resp.isResponse();
            			Address destAddress = submitSm.getSourceAddress();
            			String recordId = destAddress.getAddress();
            			String smsNumber = submitSm.getDestAddress().getAddress();
            			if(!smsNumber.startsWith("+")) {
            				smsNumber = "+"+smsNumber;
            			}
            			
            			//判断异步提交回执的一些状态
            			if(isResponse&&StringUtil.isNotBlank(recordId)&&"OK".equals(resultMessage))
        				{
            				SendRecordMapper sendRecordMapper = SpringContextUtil.getBean(SendRecordMapper.class);
        					int recordIdInt = 0;
            				try {
            					recordIdInt = Integer.valueOf(recordId).intValue(); 
    						} catch (Exception e) {}
            				if(recordIdInt != 0)
            				{
            					//先根据记录ID与通道ID。进行查询 发送记录数据；
            					//查询条件：记录ID、通道ID、状态为：提交成功、提交失败、等待提交、通道标识为空
            					LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
            					queryWrapper.eq(SendRecord::getChannelId, channelIdInt);
            					queryWrapper.eq(SendRecord::getId, recordIdInt);
            					List<Integer> stateList = new ArrayList<Integer>();
                		        stateList.add(SendRecordStateEnums.ReqSuccess.getCode());
                		        stateList.add(SendRecordStateEnums.ReqFail.getCode());
                		        stateList.add(SendRecordStateEnums.WaitReq.getCode());
                		        queryWrapper.in(SendRecord::getState, stateList);
                		        queryWrapper.isNull(SendRecord::getResMsgid);
            					SendRecord querySendRecord = null;
            					try {
            						querySendRecord = sendRecordMapper.selectOne(queryWrapper);
    							} catch (Exception e) {
    								// TODO: handle exception
    							}
            					
            					//判断根据上述条件查询的结果是否存在
            					if(querySendRecord != null)
            					{
            						int smsWords = querySendRecord.getSmsWords();
            						//判断该记录的短信内容字数是否超过70：（因为发送时，字数超过70的，都默认走同步提交方式）
            			    	    if(smsWords <= 70)
            			    	    {
            			    	    	LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
            			    	    	//
            			    	    	if(StringUtil.isNotBlank(resMsgid))
                        				{
                        					log.info(String.format("【异步提交】批次号【%s】记录ID【%s】手机号码【%s】通道ID【%s】通道标识【%s】发送smpp【成功】:%s",querySendRecord.getSendCode(),recordId,smsNumber,channelIdInt,resMsgid,respJson));
                        					updateWrapper.set(SendRecord::getStateDesc, "");
                            		        updateWrapper.set(SendRecord::getResMsgid, resMsgid);
                            		        updateWrapper.set(SendRecord::getState, SendRecordStateEnums.ReqSuccess.getCode());
                        				}
                        				else
                        				{
                        					log.info(String.format("【异步提交】批次号【%s】记录ID【%s】手机号码【%s】通道ID【%s】发送smpp【请求成功，但未返回ID】:%s",querySendRecord.getSendCode(),recordId,smsNumber,channelIdInt,respJson));
                            				updateWrapper.set(SendRecord::getStateDesc, "未返回ID");
                            		        updateWrapper.set(SendRecord::getState, SendRecordStateEnums.ReqFail.getCode());
                        				}
            			    	    	if(querySendRecord.getSubmitTime()==null)
            			    	    	{
            			    	    		updateWrapper.set(SendRecord:: getSubmitTime, new Date());
            			    	    	}
            			    	    	updateWrapper.eq(SendRecord::getId, recordIdInt);
                        		        updateWrapper.in(SendRecord::getState, stateList);
                        		        updateWrapper.eq(SendRecord::getChannelId, channelIdInt);
                        		        updateWrapper.isNull(SendRecord::getResMsgid);
                        		        try {
                        		        	sendRecordMapper.update(null,updateWrapper);
                						} catch (Exception e) {
                							log.error(String.format("【异步提交回执】更新异常：通道ID【%s】通道标识【%s】手机号码【%s】记录ID【%s】", channelId,resMsgid,smsNumber,recordId));
                						} 
            			    	    }
            			    	    else
                        			{
                    					log.info(String.format("【异步提交回执】字符长度超过70，无需处理：通道ID【%s】通道标识【%s】手机号码【%s】记录ID【%s】", channelId,resMsgid,smsNumber,recordId));
                        			}
            					}
            					else
                    			{
                					log.error(String.format("【异步提交回执】符合条件的发送记录不存在：通道ID【%s】通道标识【%s】手机号码【%s】记录ID【%s】", channelId,resMsgid,smsNumber,recordId));
                    			}
            				}
            				else
                			{
            					log.error(String.format("【异步提交回执】记录ID不符合条件：通道ID【%s】通道标识【%s】手机号码【%s】记录ID【%s】", channelId,resMsgid,smsNumber,recordId));
                			}
        				}
            			else
            			{
        					log.error(String.format("【异步提交回执】条件不成立：通道ID【%s】通道标识【%s】手机号码【%s】记录ID【%s】isResponse【%s】resultMessage【%s】", channelId,resMsgid,smsNumber,recordId,isResponse,resultMessage));
            			}
					} catch (Exception e) {
						log.error(String.format("【异步提交回执】解析异常", e.getMessage()));
					}
    	    	}
    	    	else
    	    	{
        			try {
        				log.info("[常规]收到[上游]SubmitSmResp的消息："+JSON.toJSONString(msg));
        			} catch (Exception e) {
        				log.info("[常规]NO1收到[上游]SubmitSmResp的消息转换JSON失败");
        				try {
        					log.info("[常规]NO2收到[上游]SubmitSmResp的消息："+ msg);
        				} catch (Exception e1) {
        					log.info("[常规]NO3收到[上游]SubmitSmResp的消息输出失败");
        				}
        			}
        	    }
    		}
    	    else
    	    {
    			try {
    				log.info("[常规]收到[上游]SubmitSmResp的消息："+JSON.toJSONString(msg));
    			} catch (Exception e) {
    				log.info("[常规]NO1收到[上游]SubmitSmResp的消息转换JSON失败");
    				try {
    					log.info("[常规]NO2收到[上游]SubmitSmResp的消息："+ msg);
    				} catch (Exception e1) {
    					log.info("[常规]NO3收到[上游]SubmitSmResp的消息输出失败");
    				}
    			}
    	    }
    	    ctx.fireChannelRead(msg);
		}
		else {
			try {
				log.info("[常规]收到[上游]的消息："+JSON.toJSONString(msg));
			} catch (Exception e) {
				log.info("[常规]NO1收到[上游]的消息转换JSON失败");
				try {
					log.info("[常规]NO2收到[上游]的消息："+ msg);
				} catch (Exception e1) {
					log.info("[常规]NO3收到[上游]的消息输出失败");
				}
			}
			ctx.fireChannelRead(msg);
		}
	}

	
}
