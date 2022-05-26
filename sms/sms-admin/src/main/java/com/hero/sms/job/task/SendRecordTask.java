package com.hero.sms.job.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoQuery;
import com.hero.sms.entity.message.SendRecordCheckinfoPlanQuery;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordCheckinfoService;
import com.hero.sms.service.message.ISendRecordCheckinfoPlanService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 发送记录重推超时<等待提交><提交失败>的记录任务
 * 
 * @author Lenovo
 *
 */
@Slf4j
@Component
public class SendRecordTask {

	@Autowired
	private ISendBoxService sendBoxService;

    @Autowired
    private ISendRecordService sendRecordService;
    
    @Autowired
    private ISmsChannelService smsChannelService;
    
    @Autowired
    private ISendRecordCheckinfoService sendRecordCheckinfoService;

    @Autowired
    private ISendRecordCheckinfoPlanService sendRecordCheckinfoPlanService;
    
	public void run() 
	{
		String autoAgainSendRecordSwitch = "OFF";
		Code autoAgainSendRecordSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainSendRecordSwitch");
	    if(autoAgainSendRecordSwitchCode!=null&&!"".equals(autoAgainSendRecordSwitchCode.getName()))
	    {
	    	autoAgainSendRecordSwitch = autoAgainSendRecordSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainSendRecordSwitch))
	    {
	    	 SendRecordQuery querySendRecord = new SendRecordQuery();
	    	 //通道ID不为空
	    	 querySendRecord.setIsChannelIdFlag(true);
	    	 //消费金额不为空
	    	 querySendRecord.setIsConsumeAmountFlag(true);
	    	 //消费金额不为0
	    	 querySendRecord.setNotIsConsumeAmount(0);
		     //指定状态为<等待提交><提交失败>
	    	 //是否加入<提交失败>的状态
	    	 String sendRecordStatesFlag = "0";
	    	 Code sendRecordFlagCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStatesFlag");
		     if(sendRecordFlagCode!=null&&!"".equals(sendRecordFlagCode.getName()))
		     {
		    	 sendRecordStatesFlag = sendRecordFlagCode.getName();
		     }
			 Integer[] states = null;
			 if("0".equals(sendRecordStatesFlag))
			 {
				 states = new Integer[1];
				 states[0] = SendRecordStateEnums.WaitReq.getCode();
			 }
			 else if("1".equals(sendRecordStatesFlag))
			 {
				 states = new Integer[1];
				 states[0] = SendRecordStateEnums.ReqFail.getCode();
			 }
			 else if("2".equals(sendRecordStatesFlag))
			 {
			 	states = new Integer[2];
			 	states[0] = SendRecordStateEnums.WaitReq.getCode();
			 	states[1] = SendRecordStateEnums.ReqFail.getCode();
			 }
			 else
			 {
			    states = new Integer[1];
				states[0] = SendRecordStateEnums.WaitReq.getCode();
			 }
//		     Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
		     querySendRecord.setStateArray(states);
	        //获取当前时间
	        Date createTime = new Date();
	        //默认查询开始时间为：当前时间前6个小时（单位：分）
	        int sortingStartTimeInt = -60*3;
	        Code sortingStartTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStartTimeInt");
	        if(sortingStartTimeIntCode!=null&&!"".equals(sortingStartTimeIntCode.getName()))
	        {
	        	sortingStartTimeInt = Integer.parseInt(sortingStartTimeIntCode.getName());
	        }
	        Date createStartTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingStartTimeInt);
	        querySendRecord.setCreateStartTime(createStartTime);
	       
	        //默认查询结束时间为：当前时间前15分钟（单位：分）
	        int sortingEndTimeInt = -15;
	        Code sortingEndTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordEndTimeInt");
	        if(sortingEndTimeIntCode!=null&&!"".equals(sortingEndTimeIntCode.getName()))
	        {
	        	sortingEndTimeInt = Integer.parseInt(sortingEndTimeIntCode.getName());
	        }
	        Date createEndTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingEndTimeInt);
	        querySendRecord.setCreateEndTime(createEndTime);
	        //排序方式，按创建时间升序排序
	        querySendRecord.setOrderByCreateTimeWay("ASC");
	        querySendRecord.setResMsgidIsNullFlag(true);
//	        QueryRequest queryRequest = new QueryRequest();
//			queryRequest.setPageSize(500);
//			loopQuery(queryRequest,querySendRecord);
	        loopQueryAll(querySendRecord);
	    }
	    else
	    {
	    	log.info("SendRecordTask任务开关未开启NO1");
	    }
	}
	
	private void loopQueryAll(SendRecordQuery query) 
	{
		String autoAgainSendRecordSwitch = "OFF";
		Code autoAgainSendRecordSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainSendRecordSwitch");
	    if(autoAgainSendRecordSwitchCode!=null&&!"".equals(autoAgainSendRecordSwitchCode.getName()))
	    {
	    	autoAgainSendRecordSwitch = autoAgainSendRecordSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainSendRecordSwitch))
	    {
	    	log.info("SendRecordTask任务:开始查询记录");
	    	List<SendRecord> sendRecords = sendRecordService.findSendRecords(query);
	        if (sendRecords != null && sendRecords.size()>0) 
	        {
	        	log.info("SendRecordTask任务:记录条数："+sendRecords.size());
	        	List<SmsChannel> smsChannels = smsChannelService.findSmsChannels(new SmsChannel());
	        	log.info("SendRecordTask任务:查询通道");
	 	        if(CollectionUtils.isEmpty(smsChannels)) 
	 	        {
	 	        	log.info("SendRecordTask目前没有可用通道NO5");
	 	        }
	 	        else
	 	        {
	 	        	//指定状态为<等待提交><提交失败>
	 		    	 //是否加入<提交失败>的状态
	 		    	 String sendRecordStatesFlag = "0";
	 		    	 Code sendRecordFlagCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStatesFlag");
	 			     if(sendRecordFlagCode!=null&&!"".equals(sendRecordFlagCode.getName()))
	 			     {
	 			    	 sendRecordStatesFlag = sendRecordFlagCode.getName();
	 			     }
	 			     log.info("SendRecordTask任务:根据设定的续处理的状态类型sendRecordStatesFlag："+ sendRecordStatesFlag);
	 				 if("1".equals(sendRecordStatesFlag)||"2".equals(sendRecordStatesFlag))
	 				 {
	 					String sendRecordCheckinfoSwitch = "ON";
	 			 		Code sendRecordCheckinfoSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckinfoSwitch");
	 			 	    if(sendRecordCheckinfoSwitchCode!=null&&!"".equals(sendRecordCheckinfoSwitchCode.getName()))
	 			 	    {
	 			 	    	sendRecordCheckinfoSwitch = sendRecordCheckinfoSwitchCode.getName();
	 			 	    }
	 			 	    log.info("SendRecordTask任务:校验是否需要解锁sendRecordCheckinfoSwitch："+ sendRecordStatesFlag);
	 			 	    if(!"OFF".equals(sendRecordCheckinfoSwitch))
	 			 	    {
	 			 	    	String showRunTimeLogSwitch = "OFF";
	 				 		Code showRunTimeLogSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogSwitch");
	 				 	    if(showRunTimeLogSwitchCode!=null&&!"".equals(showRunTimeLogSwitchCode.getName()))
	 				 	    {
	 				 	    	showRunTimeLogSwitch = showRunTimeLogSwitchCode.getName();
	 				 	    }
	 				 	    log.info("SendRecordTask任务:开始解锁");
	 			 	    	//状态为（提交失败）的记录，先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
	 				 	    List<Long> idsList = new ArrayList<Long>();
	 			        	StringBuffer smsNumber = new StringBuffer();
	 			        	for (int i = 0; i < sendRecords.size(); i++) 
	 			        	{
	 			        		SendRecord reSendRecord = sendRecords.get(i);
	 			        		idsList.add(reSendRecord.getId());
	 			        		if(i == 0)
	 			        		{
	 			        			smsNumber.append(reSendRecord.getSmsNumber());
	 			        		}
	 			        		else
	 			        		{
	 			        			smsNumber.append(",").append(reSendRecord.getSmsNumber());
	 			        		}
	 						}

	 	        	    	String checkSendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
	 	        	    	
		 	        		String isForceReSendSwitch = "OFF";
	 				 		Code isForceReSendSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","isForceReSendSwitch");
	 				 	    if(isForceReSendSwitchCode!=null&&!"".equals(isForceReSendSwitchCode.getName()))
	 				 	    {
	 				 	    	isForceReSendSwitch = isForceReSendSwitchCode.getName();
	 				 	    }
	 				 	    String thisdate = "0";
	 				 	    if("ON".equals(isForceReSendSwitch))
	 				 	    {
	 				 	    	thisdate = DateUtils.getString("MMddHHmmss");
	 				 	    }
	 				 	    
	 				 	    if(idsList!=null&&idsList.size()>0)
	 			        	{
	 			        		String sendRecordCheckInfoWay = "0";
	 			        		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
	 			        	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
	 			        	    {
	 			        	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
	 			        	    }
	 			        	    log.info("SendRecordTask任务:解锁方式sendRecordCheckInfoWay"+sendRecordCheckInfoWay);
	 			        	    //1、通过记录id校验； 0或其他 通过批次号与号码校验
	 			        	    if("1".equals(sendRecordCheckInfoWay))
	 			        	    {
	 			        	    	SendRecordCheckinfoPlanQuery sendRecordCheckinfoPlan = new SendRecordCheckinfoPlanQuery();
	 			                	sendRecordCheckinfoPlan.setState(1);
	 			                	sendRecordCheckinfoPlan.setStates(thisdate);
	 			                	sendRecordCheckinfoPlan.setIdsList(idsList);
	 			                	try 
	 			                	{
	 			                		sendRecordCheckinfoPlanService.updateSendRecordCheckinfoPlanBatch(sendRecordCheckinfoPlan);
	 			        			} catch (Exception e) {
	 			        				e.printStackTrace();
	 			        				log.info(String.format("B-SendRecordTask：号码【%s】解锁异常",smsNumber.toString()));
	 			        			}
	 			        	    }
	 			        	    else
	 			        	    {
	 			        	    	SendRecordCheckinfoQuery sendRecordCheckinfo = new SendRecordCheckinfoQuery();
	 			            		sendRecordCheckinfo.setState(1);
	 			            		sendRecordCheckinfo.setStates(thisdate);
	 			                	sendRecordCheckinfo.setIdsList(idsList);
	 			                	try 
	 			                	{
	 			            			sendRecordCheckinfoService.updateSendRecordCheckinfoBatch(sendRecordCheckinfo);
	 			        			} catch (Exception e) {
	 			        				e.printStackTrace();
	 			        				log.info(String.format("A-SendRecordTask：号码【%s】解锁异常",smsNumber.toString()));
	 			        			}
	 			        	    }  
	 			        	   log.info("SendRecordTask任务:完成解锁");
	 			        	}
			 	    	    if("ON".equals(showRunTimeLogSwitch))
			 	   	 	    {
			 	    	    	String checkSendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			 	    	    	long checkSendRecordRuntime = DateUtil.getTime(checkSendRecordBeginTime, checkSendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
			 	   	 	    	log.error(String.format("【重发任务记录解锁耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",smsNumber.toString(),checkSendRecordBeginTime,checkSendRecordEndTime,String.valueOf(checkSendRecordRuntime)));
			 	   	 	    }
	 			 	    }
	 				 }
	 				 log.info("SendRecordTask任务:提交到MQ进行推送");
	 	        	 int successNum = sendBoxService.pushMsg(smsChannels,sendRecords);
	 	        	 log.info(String.format("SendRecordTask成功重发%s条记录",successNum));
	 	        }
	        }
	        else
	        {
	        	log.info("SendRecordTask无超时或提交失败的发送记录NO3");
	        }
	    }
	    else
	    {
	    	log.info("SendRecordTask任务开关未开启NO2");
	    }
	}
	
	public void runChannel(String channelId) 
	{
		log.info("SendRecordTask任务根据【通道ID】筛选的方法");
		String autoAgainSendRecordSwitch = "OFF";
		Code autoAgainSendRecordSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainSendRecordSwitch");
	    if(autoAgainSendRecordSwitchCode!=null&&!"".equals(autoAgainSendRecordSwitchCode.getName()))
	    {
	    	autoAgainSendRecordSwitch = autoAgainSendRecordSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainSendRecordSwitch))
	    {
	    	 SendRecordQuery querySendRecord = new SendRecordQuery();
	    	 //通道ID不为空
	    	 querySendRecord.setIsChannelIdFlag(true);
	    	 //消费金额不为空
	    	 querySendRecord.setIsConsumeAmountFlag(true);
	    	 //消费金额不为0
	    	 querySendRecord.setNotIsConsumeAmount(0);
		     //指定状态为<等待提交><提交失败>
	    	 //是否加入<提交失败>的状态
	    	 String sendRecordStatesFlag = "0";
	    	 Code sendRecordFlagCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStatesFlag");
		     if(sendRecordFlagCode!=null&&!"".equals(sendRecordFlagCode.getName()))
		     {
		    	 sendRecordStatesFlag = sendRecordFlagCode.getName();
		     }
			 Integer[] states = null;
			 if("0".equals(sendRecordStatesFlag))
			 {
				 states = new Integer[1];
				 states[0] = SendRecordStateEnums.WaitReq.getCode();
			 }
			 else if("1".equals(sendRecordStatesFlag))
			 {
				 states = new Integer[1];
				 states[0] = SendRecordStateEnums.ReqFail.getCode();
			 }
			 else if("2".equals(sendRecordStatesFlag))
			 {
			 	states = new Integer[2];
			 	states[0] = SendRecordStateEnums.WaitReq.getCode();
			 	states[1] = SendRecordStateEnums.ReqFail.getCode();
			 }
			 else
			 {
			    states = new Integer[1];
				states[0] = SendRecordStateEnums.WaitReq.getCode();
			 }
//		     Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
		     querySendRecord.setStateArray(states);
	        //获取当前时间
	        Date createTime = new Date();
	        //默认查询开始时间为：当前时间前6个小时（单位：分）
	        int sortingStartTimeInt = -60*3;
	        Code sortingStartTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStartTimeInt");
	        if(sortingStartTimeIntCode!=null&&!"".equals(sortingStartTimeIntCode.getName()))
	        {
	        	sortingStartTimeInt = Integer.parseInt(sortingStartTimeIntCode.getName());
	        }
	        Date createStartTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingStartTimeInt);
	        querySendRecord.setCreateStartTime(createStartTime);
	       
	        //默认查询结束时间为：当前时间前15分钟（单位：分）
	        int sortingEndTimeInt = -15;
	        Code sortingEndTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordEndTimeInt");
	        if(sortingEndTimeIntCode!=null&&!"".equals(sortingEndTimeIntCode.getName()))
	        {
	        	sortingEndTimeInt = Integer.parseInt(sortingEndTimeIntCode.getName());
	        }
	        Date createEndTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingEndTimeInt);
	        querySendRecord.setCreateEndTime(createEndTime);
	        //排序方式，按创建时间升序排序
	        querySendRecord.setOrderByCreateTimeWay("ASC");
	        querySendRecord.setResMsgidIsNullFlag(true);
	        if(StringUtil.isNotBlank(channelId))
	        {
	        	try {
					int channelIdInt = Integer.valueOf(channelId).intValue();
					querySendRecord.setChannelId(channelIdInt);
					loopQueryAll(querySendRecord);
				} catch (Exception e) {}
	        }
	    }
	    else
	    {
	    	log.info("SendRecordTask任务开关未开启NO1");
	    }
	}
	
	public void runOrg(String orgCode) 
	{
		log.info("SendRecordTask任务根据【商户编号】筛选的方法");
		String autoAgainSendRecordSwitch = "OFF";
		Code autoAgainSendRecordSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainSendRecordSwitch");
	    if(autoAgainSendRecordSwitchCode!=null&&!"".equals(autoAgainSendRecordSwitchCode.getName()))
	    {
	    	autoAgainSendRecordSwitch = autoAgainSendRecordSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainSendRecordSwitch))
	    {
	    	 SendRecordQuery querySendRecord = new SendRecordQuery();
	    	 //通道ID不为空
	    	 querySendRecord.setIsChannelIdFlag(true);
	    	 //消费金额不为空
	    	 querySendRecord.setIsConsumeAmountFlag(true);
	    	 //消费金额不为0
	    	 querySendRecord.setNotIsConsumeAmount(0);
		     //指定状态为<等待提交><提交失败>
	    	 //是否加入<提交失败>的状态
	    	 String sendRecordStatesFlag = "0";
	    	 Code sendRecordFlagCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStatesFlag");
		     if(sendRecordFlagCode!=null&&!"".equals(sendRecordFlagCode.getName()))
		     {
		    	 sendRecordStatesFlag = sendRecordFlagCode.getName();
		     }
			 Integer[] states = null;
			 if("0".equals(sendRecordStatesFlag))
			 {
				 states = new Integer[1];
				 states[0] = SendRecordStateEnums.WaitReq.getCode();
			 }
			 else if("1".equals(sendRecordStatesFlag))
			 {
				 states = new Integer[1];
				 states[0] = SendRecordStateEnums.ReqFail.getCode();
			 }
			 else if("2".equals(sendRecordStatesFlag))
			 {
			 	states = new Integer[2];
			 	states[0] = SendRecordStateEnums.WaitReq.getCode();
			 	states[1] = SendRecordStateEnums.ReqFail.getCode();
			 }
			 else
			 {
			    states = new Integer[1];
				states[0] = SendRecordStateEnums.WaitReq.getCode();
			 }
//		     Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
		     querySendRecord.setStateArray(states);
	        //获取当前时间
	        Date createTime = new Date();
	        //默认查询开始时间为：当前时间前6个小时（单位：分）
	        int sortingStartTimeInt = -60*3;
	        Code sortingStartTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordStartTimeInt");
	        if(sortingStartTimeIntCode!=null&&!"".equals(sortingStartTimeIntCode.getName()))
	        {
	        	sortingStartTimeInt = Integer.parseInt(sortingStartTimeIntCode.getName());
	        }
	        Date createStartTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingStartTimeInt);
	        querySendRecord.setCreateStartTime(createStartTime);
	       
	        //默认查询结束时间为：当前时间前15分钟（单位：分）
	        int sortingEndTimeInt = -15;
	        Code sortingEndTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordEndTimeInt");
	        if(sortingEndTimeIntCode!=null&&!"".equals(sortingEndTimeIntCode.getName()))
	        {
	        	sortingEndTimeInt = Integer.parseInt(sortingEndTimeIntCode.getName());
	        }
	        Date createEndTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingEndTimeInt);
	        querySendRecord.setCreateEndTime(createEndTime);
	        //排序方式，按创建时间升序排序
	        querySendRecord.setOrderByCreateTimeWay("ASC");
	        querySendRecord.setResMsgidIsNullFlag(true);
	        if(StringUtil.isNotBlank(orgCode))
	        {
	        	querySendRecord.setOrgCode(orgCode);
	        	loopQueryAll(querySendRecord);
	        }
	    }
	    else
	    {
	    	log.info("SendRecordTask任务开关未开启NO1");
	    }
	}
}
