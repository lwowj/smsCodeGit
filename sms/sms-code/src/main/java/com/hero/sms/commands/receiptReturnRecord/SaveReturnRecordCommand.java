package com.hero.sms.commands.receiptReturnRecord;

import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.impl.channel.push.BaseSmsPushService;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 保存回执记录
 * @author Lenovo
 *
 */
@Slf4j
public class SaveReturnRecordCommand extends BaseCommand {

	@Override
	public boolean execute(Context context) throws Exception {
		IReturnRecordService returnRecordService = (IReturnRecordService)context.get(OBJ_RETURN_RECORD_SERVICE);
		ISendRecordService sendRecordService = (ISendRecordService)context.get(OBJ_SENDRECORD_SERVICE);
		BaseSmsPushService pushService = (BaseSmsPushService)context.get(OBJ_PUSH_SERVICE);
		String resultData = (String)context.get(STR_RESULT_DATA);
		SmsChannelExt smsChannel = (SmsChannelExt)context.get(OBJ_SMS_CHANNEL);
		String supportArea = smsChannel.getSupportArea();
		
        List<ReturnRecord> returnRecords = pushService.receipt(resultData);
        List<SendRecord> notifySendRecord = Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(returnRecords)) {
        	for (ReturnRecord returnRecord : returnRecords) {
        		try {
					String smsNumber = null;
        			SendRecord sendRecord = null;
        			boolean needResMsgidQueryFlag = true;
        	 	    if(StringUtil.isNotBlank(supportArea))
        	 	    {
        	 	    	if(StringUtil.isNotBlank(returnRecord.getSmsNumber()))
        	 	    	{
        	 	    		if(smsNumber.startsWith(supportArea)) smsNumber = StringUtils.removeStart(returnRecord.getSmsNumber(), supportArea);
            				else smsNumber = StringUtils.removeStart(returnRecord.getSmsNumber(), "+");
        	 	    		needResMsgidQueryFlag = false;
        	 	    	}
        	 	    }
        	 	    
        	 	    if(needResMsgidQueryFlag)
        	 	    {
        	 	    	sendRecord = sendRecordService.getByChannelIdAndMsgId(returnRecord.getChannelId(),returnRecord.getResMsgid());
        	 	    	if(sendRecord != null) 
        	 	    	{
        	 	    		smsNumber = sendRecord.getSmsNumber();
        	 	    		supportArea = sendRecord.getSmsNumberArea();
        	 	    	}
        	 	    }
        	 	    else
        	 	    {
        	 	    	sendRecord = sendRecordService.getByAreaAndNumberAndMsgId(supportArea,smsNumber,returnRecord.getResMsgid());
        	 	    }
					
					if(sendRecord != null) {
						returnRecord.setSmsNumber(smsNumber);
						returnRecord.setAgentId(sendRecord.getAgentId());
						returnRecord.setChannelId(sendRecord.getChannelId());
						returnRecord.setOrgCode(sendRecord.getOrgCode());
						returnRecord.setReqCreateTime(sendRecord.getCreateTime());
						returnRecord.setReqDesc(sendRecord.getStateDesc());
						returnRecord.setReqState(sendRecord.getState());
						returnRecord.setSendCode(sendRecord.getSendCode());
						returnRecord.setSmsContent(sendRecord.getSmsContent());
						returnRecord.setSmsCount(sendRecord.getSmsCount());
						returnRecord.setSmsNumberArea(sendRecord.getSmsNumberArea());
						returnRecord.setSmsType(sendRecord.getSmsType());
						returnRecord.setSmsWords(sendRecord.getSmsWords());
						
//						if(sendRecord.getState().intValue() == SendRecordStateEnums.ReqSuccess.getCode()) 
						/**
						 * 2021-04-05
						 * 新增提交失败状态：一般提交失败情况为网络不同或上游无返回；
						 * 上游无返回情况，可能上游已经完成发送，故会有回执反馈，所以不能只以提交成功状态为发送成功标准
						 */
						if(sendRecord.getState().intValue() == SendRecordStateEnums.ReqSuccess.getCode()||sendRecord.getState().intValue() == SendRecordStateEnums.ReqFail.getCode())
						{
							int receiptState = SendRecordStateEnums.ReceiptSuccess.getCode();
							if(returnRecord.getReturnState().intValue() == CommonStateEnums.FAIL.getCode().intValue()) {
								receiptState = SendRecordStateEnums.ReceiptFail.getCode();
							}
							sendRecord.setState(receiptState);
							notifySendRecord.add(sendRecord);
							
							LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
							updateWrapper.set(SendRecord::getState, receiptState);
							updateWrapper.set(SendRecord::getReceiptTime, new Date());
							updateWrapper.eq(SendRecord::getId, sendRecord.getId());
							sendRecordService.update(updateWrapper);
						}
					}
				} catch (Exception e) {
					log.error("保存回执信息失败",e);
					context.put(STR_ERROR_INFO, "保存回执信息失败");
					return true;
				}
			}
        	returnRecordService.saveBatch(returnRecords);
        	context.put(LIST_SEND_RECORD, notifySendRecord);
        }
		return false;
	}

}
