package com.hero.sms.service.impl.channel.push;

import java.net.InetSocketAddress;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.utils.RandomUtil;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SMPPServerMessageReceiveHandler extends AbstractBusinessHandler {

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
		if (msg instanceof SubmitSm) {
			try {
				log.info("【对接】收到[下游]提交发送格式的消息："+JSON.toJSONString(msg));
			} catch (Exception e) {
				log.info("【对接】NO1收到[下游]提交发送格式的消息转换JSON失败");
				try {
					log.info("【对接】NO2收到[下游]提交发送格式的消息："+ msg);
				} catch (Exception e1) {
					log.info("【对接】NO3收到[下游]提交发送格式的消息输出失败");
				}
			}
			long start = System.currentTimeMillis();
			try {
				SubmitSm submitSm = (SubmitSm) msg;
				SMPPServerChildEndpointEntity entity = (SMPPServerChildEndpointEntity)getEndpointEntity();
				String systemId = entity.getSystemId();
				InetSocketAddress ipSocket = (InetSocketAddress)ctx.channel().remoteAddress();
			    String clientIp = ipSocket.getAddress().getHostAddress();
				String organizationCode = entity.getId();
				Organization organization = DatabaseCache.getOrganizationByOrgcode(organizationCode);
				ISendBoxService sendBoxService = SpringContextUtil.getBean(ISendBoxService.class);
				SendBox sendBox = new SendBox();
				//商户等相关信息初始化
				sendBox.setClientIp(clientIp);
				sendBox.setSubType(SendBoxSubTypeEnums.SmppSub.getCode());
				sendBox.setOrgCode(organizationCode);
				sendBox.setCreateUsername(systemId);
				sendBox.setAgentId(organization.getAgentId());
				sendBox.setSmsType(SmsTypeEnums.TextMsg.getCode());
				sendBox.setType(SendBoxTypeEnums.formSubmit.getCode());
				Address destAddress = submitSm.getDestAddress();
				String smsNumbers = destAddress.getAddress();
				String[] areaAndNumber = SmsNumberAreaCodeEnums.splitAreaAndNumber(smsNumbers);
				if(areaAndNumber == null) {
					String msgId = RandomUtil.randomStringWithDate(5);
					SubmitSmResp res = ((SubmitSm) msg).createResponse();
					res.setMessageId(msgId);
					
					DeliverSmReceipt report = new DeliverSmReceipt();
					report.setId(msgId);
					report.setSourceAddress(((SubmitSm) msg).getDestAddress());
					report.setDestAddress(((SubmitSm) msg).getSourceAddress());
					report.setStat("UNKNOWN");
					report.setText("手机号码格式错误");
					String currDate = DateUtil.getString(new Date(), "yyyyMMddHHmm");
					report.setSubmit_date(currDate);
					report.setDone_date(currDate);
					ctx.writeAndFlush(report);
					sendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
					sendBox.setSmsNumbers(smsNumbers);
				}
				else
				{
					sendBox.setSmsNumberArea(areaAndNumber[0]);
					sendBox.setSmsNumbers(areaAndNumber[1]);
				}
				sendBox.setNumberCount(1);
				sendBox.setSmsContent(submitSm.getMsgContent());
				sendBoxService.createSendBox(sendBox);
				if(sendBox.getAuditState().intValue() == AuditStateEnums.Pass.getCode().intValue()) {
					//6-1-1-1 将短信内容进行拆分，之后推送到MQ
		        	sendBoxService.splitRecordForTxt(sendBox, Lists.newArrayList(areaAndNumber[1]));
		        }
				
				//6-1-1-2  以下为smpp的响应方式（即接收到下游参数后，smpp对应给予结果响应）
				SubmitSmResp res = ((SubmitSm) msg).createResponse();
				res.setMessageId(sendBox.getSendCode());
				ctx.writeAndFlush(res);
				log.info(String.format("【%s】耗时【%d】",areaAndNumber[1],(System.currentTimeMillis()-start)));
			} catch (ServiceException e) {
				log.error("smpp保存短信业务异常",e);
				String msgId = RandomUtil.randomStringWithDate(5);
				SubmitSmResp res = ((SubmitSm) msg).createResponse();
				res.setMessageId(msgId);
				ctx.writeAndFlush(res);
				
				DeliverSmReceipt report = new DeliverSmReceipt();
				report.setId(msgId);
				report.setSourceAddress(((SubmitSm) msg).getDestAddress());
				report.setDestAddress(((SubmitSm) msg).getSourceAddress());
				report.setStat("REJECTED");
				report.setText(e.getMessage());
				String currDate = DateUtil.getString(new Date(), "yyyyMMddHHmm");
				report.setSubmit_date(currDate);
				report.setDone_date(currDate);
				ctx.writeAndFlush(report);
			} catch (Exception e) {
				log.error("smpp保存短信异常",e);
				String msgId = RandomUtil.randomStringWithDate(5);
				SubmitSmResp res = ((SubmitSm) msg).createResponse();
				res.setMessageId(msgId);
				ctx.fireChannelRead(msg);
				
				DeliverSmReceipt report = new DeliverSmReceipt();
				report.setId(msgId);
				report.setSourceAddress(((SubmitSm) msg).getDestAddress());
				report.setDestAddress(((SubmitSm) msg).getSourceAddress());
				report.setStat("REJECTED");
				report.setText("保存失败");
				String currDate = DateUtil.getString(new Date(), "yyyyMMddHHmm");
				report.setSubmit_date(currDate);
				report.setDone_date(currDate);
				ctx.writeAndFlush(report);
			}
		}
		else
		{
			try {
				log.info("[常规]收到[下游]提交发送格式的消息："+JSON.toJSONString(msg));
			} catch (Exception e) {
				log.info("[常规]NO1收到[下游]提交发送格式的消息转换JSON失败");
				try {
					log.info("[常规]NO2收到[下游]提交发送格式的消息："+ msg);
				} catch (Exception e1) {
					log.info("[常规]NO3收到[下游]提交发送格式的消息输出失败");
				}
			}
		}
	}
	
}
