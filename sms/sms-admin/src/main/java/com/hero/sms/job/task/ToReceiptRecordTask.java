package com.hero.sms.job.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormal;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormalQuery;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.ProcessingStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.message.IReceiptReturnRecordAbnormalService;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 上游回执接收处理失败，二次重推接收任务
 * 
 * @author Lenovo
 *
 */
@Slf4j
@Component
public class ToReceiptRecordTask {

    @Autowired
    private IReceiptReturnRecordAbnormalService receiptReturnRecordAbnormalService;

    @Autowired
    private IReturnRecordService returnRecordService;
    
    @Autowired
    private ISendRecordService sendRecordService;

	public void run() 
	{
		String autoAgainToReceiptSwitch = "OFF";
		Code autoAgainToReceiptSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainToReceiptSwitch");
	    if(autoAgainToReceiptSwitchCode!=null&&!"".equals(autoAgainToReceiptSwitchCode.getName()))
	    {
	    	autoAgainToReceiptSwitch = autoAgainToReceiptSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainToReceiptSwitch))
	    {
	    	ReceiptReturnRecordAbnormalQuery  abnormalQuery = new ReceiptReturnRecordAbnormalQuery();
	    	abnormalQuery.setProcessingState(ProcessingStateEnums.UNFINISHED.getCode().intValue());
	    	 
	        //获取当前时间
	        Date createTime = new Date();
	        //默认查询开始时间为：当前时间前3个小时（单位：分）
	        int toReceiptStartTimeInt = -60*3;
	        Code toReceiptStartTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","toReceiptStartTimeInt");
	        if(toReceiptStartTimeIntCode!=null&&!"".equals(toReceiptStartTimeIntCode.getName()))
	        {
	        	toReceiptStartTimeInt = Integer.parseInt(toReceiptStartTimeIntCode.getName());
	        }
	        Date createStartTime = DateUtils.getDate(createTime, Calendar.MINUTE, toReceiptStartTimeInt);
	        abnormalQuery.setCreateStartTime(createStartTime);
	       
	        //默认查询结束时间为：当前时间前15分钟（单位：分）
	        int toReceiptEndTimeInt = -15;
	        Code toReceiptEndTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","toReceiptEndTimeInt");
	        if(toReceiptEndTimeIntCode!=null&&!"".equals(toReceiptEndTimeIntCode.getName()))
	        {
	        	toReceiptEndTimeInt = Integer.parseInt(toReceiptEndTimeIntCode.getName());
	        }
	        Date createEndTime = DateUtils.getDate(createTime, Calendar.MINUTE, toReceiptEndTimeInt);
	        abnormalQuery.setCreateEndTime(createEndTime);
	        //排序方式，按创建时间升序排序
	        abnormalQuery.setOrderByCreateTimeWay("ASC");
	        
	        QueryRequest queryRequest = new QueryRequest();
			queryRequest.setPageSize(500);
			loopQuery(queryRequest,abnormalQuery);
	    }
	    else
	    {
	    	log.info("ToReceiptRecordTask任务开关未开启NO1");
	    }
	}
	
	private void loopQuery(QueryRequest queryRequest, ReceiptReturnRecordAbnormalQuery query) 
	{
		String autoAgainToReceiptSwitch = "OFF";
		Code autoAgainToReceiptSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainToReceiptSwitch");
	    if(autoAgainToReceiptSwitchCode!=null&&!"".equals(autoAgainToReceiptSwitchCode.getName()))
	    {
	    	autoAgainToReceiptSwitch = autoAgainToReceiptSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainToReceiptSwitch))
	    {
	    	IPage<ReceiptReturnRecordAbnormal> receiptAbnormals = receiptReturnRecordAbnormalService.findReceiptReturnRecordAbnormals(queryRequest, query);
	        if (receiptAbnormals != null && CollectionUtils.isNotEmpty(receiptAbnormals.getRecords())) 
	        {
	            List<ReceiptReturnRecordAbnormal> records = receiptAbnormals.getRecords();
	            if(records!=null&&records.size()>0)
	            {
		    		List<ReceiptReturnRecordAbnormal> updateAbnormalList = new ArrayList<>();
		    		for (int i = 0; i < records.size(); i++) 
		        	{
		    			ReceiptReturnRecordAbnormal thisAbnormal = records.get(i);
		    			
		    			ReceiptReturnRecordAbnormal updateAbnormal = new ReceiptReturnRecordAbnormal();
		        		updateAbnormal.setId(thisAbnormal.getId());
		        		
		        		SendRecord sendRecord = sendRecordService.getByAreaAndNumberAndMsgId(thisAbnormal.getSmsNumberArea(),thisAbnormal.getSmsNumber(),thisAbnormal.getResMsgid());
		        		if(sendRecord!=null)
		        		{
		        			if(sendRecord.getState()==SendRecordStateEnums.ReqSuccess.getCode())
		        			{
		        				String data  = thisAbnormal.getReturnDataparam();
		        				returnRecordService.receiptReturnRecord(JSON.parseObject(data, ReturnRecord.class),"RESMSGID="+thisAbnormal.getResMsgid());
		        			}
		        			updateAbnormal.setProcessingState(ProcessingStateEnums.FINISHED.getCode().intValue());
		        		}
		        		else
		        		{
		        			updateAbnormal.setProcessingState(ProcessingStateEnums.INVALID.getCode().intValue());
		        		}
		        		updateAbnormal.setAgainTime(new Date());
		        		updateAbnormalList.add(updateAbnormal);
					}
		    		try {
		    			receiptReturnRecordAbnormalService.updateBatchById(updateAbnormalList);
					} catch (Exception e) {
						e.printStackTrace();
					}
		    		long size = receiptAbnormals.getPages();
		            if (size > 1)
		            {
		                loopQuery(queryRequest,query);
		            }
	            }
	            else
		        {
		        	log.info("ToReceiptRecordTask无未处理记录推送二次接收回执NO2");
		        }
	        }
	        else
	        {
	        	log.info("ToReceiptRecordTask无未处理记录推送二次接收回执NO3");
	        }
	    }
	    else
	    {
	    	log.info("ToReceiptRecordTask任务开关未开启NO2");
	    }
	}
}
