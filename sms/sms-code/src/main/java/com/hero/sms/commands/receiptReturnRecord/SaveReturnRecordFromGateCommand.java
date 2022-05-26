package com.hero.sms.commands.receiptReturnRecord;

import java.util.Date;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormal;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.message.IReceiptReturnRecordAbnormalService;
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
public class SaveReturnRecordFromGateCommand extends BaseCommand {

	@Override
	public boolean execute(Context context) throws Exception {
		IReturnRecordService returnRecordService = (IReturnRecordService)context.get(OBJ_RETURN_RECORD_SERVICE);
		ISendRecordService sendRecordService = (ISendRecordService)context.get(OBJ_SENDRECORD_SERVICE);
		IReceiptReturnRecordAbnormalService receiptReturnRecordAbnormalService = (IReceiptReturnRecordAbnormalService)context.get(OBJ_RECEIPT_RETURN_RECORD_SERVICE);
		ReturnRecord returnRecord = (ReturnRecord)context.get(OBJ_RETURN_RECORD);
		SmsChannelExt smsChannel = (SmsChannelExt)context.get(OBJ_SMS_CHANNEL);
		String supportArea = smsChannel.getSupportArea();
		try {
			String showRunTimeLogSwitch = "OFF";
	 		Code showRunTimeLogSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogSwitch");
	 	    if(showRunTimeLogSwitchCode!=null&&!"".equals(showRunTimeLogSwitchCode.getName()))
	 	    {
	 	    	showRunTimeLogSwitch = showRunTimeLogSwitchCode.getName();
	 	    }
	 	    
	 	   	String smsNumber = returnRecord.getSmsNumber();
	 	   	
	 	   	/* 2021-10-22
	 	   	 * 判断通道是否设置国际区号，
	 	   	 * 若设置则走原来查询流程；
	 	   	 * 若没有设置，则表示该通道为国际通道，使用通道ID与上游通道标识ID进行查询发送记录
	 	   	 */
	 	   	SendRecord sendRecord = null;
	 	    if(StringUtil.isNotBlank(supportArea))
	 	    {
				if(smsNumber.startsWith(supportArea)) smsNumber = StringUtils.removeStart(returnRecord.getSmsNumber(), supportArea);
				else smsNumber = StringUtils.removeStart(returnRecord.getSmsNumber(), "+");
				sendRecord = sendRecordService.getByAreaAndNumberAndMsgId(supportArea,smsNumber,returnRecord.getResMsgid());
	 	    }
	 	    else
	 	    {
	 	    	sendRecord = sendRecordService.getByChannelIdAndMsgId(returnRecord.getChannelId(),returnRecord.getResMsgid());
	 	    	if(sendRecord != null) 
	 	    	{
	 	    		String thisSmsNumber = sendRecord.getSmsNumber();
	 	    		String thisSupportArea = sendRecord.getSmsNumberArea();
	 	    		if(smsNumber.startsWith(thisSupportArea)) smsNumber = StringUtils.removeStart(returnRecord.getSmsNumber(), thisSupportArea);
					else smsNumber = StringUtils.removeStart(returnRecord.getSmsNumber(), "+");
	 	    		if(!thisSmsNumber.equals(smsNumber))
	 	    		{
	 	    			sendRecord = null;
	 	    		}
	 	    	}
	 	    }
	 	   
			if(sendRecord != null) {
				
				String stateDesc = returnRecord.getReqDesc();
				
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

				/**
				 * 2021-04-05
				 * 新增提交失败状态：一般提交失败情况为网络不同或上游无返回；
				 * 上游无返回情况，可能上游已经完成发送，故会有回执反馈，所以不能只以提交成功状态为发送成功标准
				 */
				if(sendRecord.getState().intValue() == SendRecordStateEnums.ReqSuccess.getCode()||sendRecord.getState().intValue() == SendRecordStateEnums.ReqFail.getCode())
				{
					String sendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
					
					int receiptState = SendRecordStateEnums.ReceiptSuccess.getCode();
					if(returnRecord.getReturnState().intValue() == CommonStateEnums.FAIL.getCode().intValue()) {
						receiptState = SendRecordStateEnums.ReceiptFail.getCode();
					}
					sendRecord.setState(receiptState);

					LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
					updateWrapper.set(SendRecord::getState, receiptState);
					updateWrapper.set(SendRecord::getReceiptTime, new Date());
					/*
					 * 2021-05-11
					 * 是否保存上游失败原因开关
					 */
					String saveChannelFailWhySwitch = "OFF";
					Code saveChannelFailWhySwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","saveChannelFailWhySwitch");
				    if(saveChannelFailWhySwitchCode!=null&&!"".equals(saveChannelFailWhySwitchCode.getName()))
				    {
				    	saveChannelFailWhySwitch = saveChannelFailWhySwitchCode.getName();
				    }
				    if("ON".equals(saveChannelFailWhySwitch))
				    {
				    	if(returnRecord.getReturnState().intValue() == CommonStateEnums.FAIL.getCode().intValue()) 
						{
							if(StringUtil.isNotBlank(stateDesc))
							{
								updateWrapper.set(SendRecord::getStateDesc, stateDesc);
							}
							else
							{
								updateWrapper.set(SendRecord::getStateDesc, null);
							}
						}
						else
						{
							updateWrapper.set(SendRecord::getStateDesc, null);
						}
				    }
				    else
				    {
				    	updateWrapper.set(SendRecord::getStateDesc, null);
				    }
					updateWrapper.eq(SendRecord::getId, sendRecord.getId());
					sendRecordService.update(updateWrapper);
					
					if("ON".equals(showRunTimeLogSwitch))
			 	    {
						String sendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
		 				long sendRecordRuntime = DateUtil.getTime(sendRecordBeginTime, sendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
		 			    log.error(String.format("【1、回执修改sendRecord记录状态】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),sendRecordBeginTime,sendRecordEndTime,String.valueOf(sendRecordRuntime)));
			 	    }
				}
			}
			else
			{
				String startDateStr = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_1);
				log.error(String.format("NO.1-【SaveReturnRecordFromGateCommand】执行时间：【%s】回执查询结果异常：sendRecord=【%s】,supportArea=【%s】,smsNumber=【%s】,getResMsgid=【%s】", startDateStr,JSON.toJSONString(sendRecord),supportArea,smsNumber,returnRecord.getResMsgid()));
				/**
				 * @begin 2020-11-21
				 * returnRecord
				 */
				String receiptReturnRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
				try {
					ReceiptReturnRecordAbnormal receiptReturnRecordAbnormal  = new ReceiptReturnRecordAbnormal();
					receiptReturnRecordAbnormal.setChannelId(returnRecord.getChannelId());
					receiptReturnRecordAbnormal.setSmsNumber(smsNumber);
					receiptReturnRecordAbnormal.setSmsNumberArea(supportArea);
					receiptReturnRecordAbnormal.setSourceNumber(returnRecord.getSourceNumber());
					receiptReturnRecordAbnormal.setResMsgid(returnRecord.getResMsgid());
					receiptReturnRecordAbnormal.setReturnState(returnRecord.getReturnState());
					receiptReturnRecordAbnormal.setReturnDesc(returnRecord.getReturnDesc());
					receiptReturnRecordAbnormal.setReturnDataparam(JSON.toJSONString(returnRecord));
					receiptReturnRecordAbnormal.setProcessingState(0);
					receiptReturnRecordAbnormal.setDescription(String.format("【SaveReturnRecordFromGateCommand】执行时间：【%s】【sendRecordService.getByAreaAndNumberAndMsgId】返回sendRecord为空", startDateStr));
					receiptReturnRecordAbnormalService.save(receiptReturnRecordAbnormal);
				} catch (Exception e) {
					log.error(String.format("NO.0-【SaveReturnRecordFromGateCommand】执行时间：【%s】接收回执异常信息入库失败：supportArea=【%s】,smsNumber=【%s】,getResMsgid=【%s】", startDateStr,supportArea,smsNumber,returnRecord.getResMsgid()), e);
				}
				if("ON".equals(showRunTimeLogSwitch))
		 	    {
					String receiptReturnRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
	 				long receiptReturnRecordRuntime = DateUtil.getTime(receiptReturnRecordBeginTime, receiptReturnRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
	 			    log.error(String.format("【2、回执查询sendRecord记录为空，入记录异常表】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",returnRecord.getResMsgid(),returnRecord.getSmsNumber(),returnRecord.getChannelId(),receiptReturnRecordBeginTime,receiptReturnRecordEndTime,String.valueOf(receiptReturnRecordRuntime)));
		 	    }
				/**
				 * @end
				 */
				/**
				 * @begin  2020-11-16
				 * 新增当为查询到发送记录时，停顿几秒重新查询一次，确保数据完整
				 */
				String queryReturnRecordSwitch = "OFF";
				Code queryReturnRecordSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","queryReturnRecordSwitch");
			    if(queryReturnRecordSwitchCode!=null&&!"".equals(queryReturnRecordSwitchCode.getName()))
			    {
			    	queryReturnRecordSwitch = queryReturnRecordSwitchCode.getName();
			    }
			    if("ON".equals(queryReturnRecordSwitch))
			    {
			    	Code sleepIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","SaveReturnRecordFromGateCommandSleepInt");
				    if(sleepIntCode!=null&&!"".equals(sleepIntCode.getName()))
				    {
				    	int sleepInt = 3000;
				    	try {
				    		sleepInt = Integer.valueOf(sleepIntCode.getName()).intValue();
						} catch (Exception e) {}
				    	if(sleepInt>0)
				    	{
				    		try {
								Thread.sleep(sleepInt);
							} catch (InterruptedException e1) {}
				    	}
				    	sendRecord = sendRecordService.getByAreaAndNumberAndMsgId(supportArea,smsNumber,returnRecord.getResMsgid());
				    	String endDateStr = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_1);
				    	log.error(String.format("NO.2-【SaveReturnRecordFromGateCommand】执行时间：【%s】回执二次查询结果：sendRecord=【%s】,supportArea=【%s】,smsNumber=【%s】,getResMsgid=【%s】", endDateStr,JSON.toJSONString(sendRecord),supportArea,smsNumber,returnRecord.getResMsgid()));
						if(sendRecord != null) {
							
							String stateDesc = returnRecord.getReqDesc();
							
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

							if(sendRecord.getState().intValue() == SendRecordStateEnums.ReqSuccess.getCode()) {
								int receiptState = SendRecordStateEnums.ReceiptSuccess.getCode();
								if(returnRecord.getReturnState().intValue() == CommonStateEnums.FAIL.getCode().intValue()) {
									receiptState = SendRecordStateEnums.ReceiptFail.getCode();
								}
								sendRecord.setState(receiptState);

								LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
								updateWrapper.set(SendRecord::getState, receiptState);
								updateWrapper.set(SendRecord::getReceiptTime, new Date());
								/*
								 * 2021-05-11
								 * 是否保存上游失败原因开关
								 */
								String saveChannelFailWhySwitch = "OFF";
								Code saveChannelFailWhySwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","saveChannelFailWhySwitch");
							    if(saveChannelFailWhySwitchCode!=null&&!"".equals(saveChannelFailWhySwitchCode.getName()))
							    {
							    	saveChannelFailWhySwitch = saveChannelFailWhySwitchCode.getName();
							    }
							    if("ON".equals(saveChannelFailWhySwitch))
							    {
							    	if(returnRecord.getReturnState().intValue() == CommonStateEnums.FAIL.getCode().intValue()) 
									{
										if(StringUtil.isNotBlank(stateDesc))
										{
											updateWrapper.set(SendRecord::getStateDesc, stateDesc);
										}
										else
										{
											updateWrapper.set(SendRecord::getStateDesc, null);
										}
									}
									else
									{
										updateWrapper.set(SendRecord::getStateDesc, null);
									}
							    }
							    else
							    {
							    	updateWrapper.set(SendRecord::getStateDesc, null);
							    }
							    
								updateWrapper.eq(SendRecord::getId, sendRecord.getId());
								sendRecordService.update(updateWrapper);
							}
						}
						else
						{
							log.error(String.format("NO.3-【SaveReturnRecordFromGateCommand】回执二次查询结果异常：sendRecord=【%s】,supportArea=【%s】,smsNumber=【%s】,getResMsgid=【%s】", JSON.toJSONString(sendRecord),supportArea,smsNumber,returnRecord.getResMsgid()));
						}
				    }
			    }
			    /**
			     * @end
			     */
			}
//			returnRecord.setCreateTime(new Date());
			String returnRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			
			returnRecordService.save(returnRecord);
			
			if("ON".equals(showRunTimeLogSwitch))
	 	    {
				String returnRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
				long returnRecordRuntime = DateUtil.getTime(returnRecordBeginTime, returnRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
				log.error(String.format("【3、保存回执记录】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",returnRecord.getResMsgid(),returnRecord.getSmsNumber(),returnRecord.getChannelId(),returnRecordBeginTime,returnRecordEndTime,String.valueOf(returnRecordRuntime)));
	 	    }
			context.put(LIST_SEND_RECORD, Lists.newArrayList(sendRecord));
		} catch (Exception e) {
			log.error("保存回执信息失败",e);
			context.put(STR_ERROR_INFO, "保存回执信息失败");
			return true;
        }
		return false;
	}

}
