package com.hero.sms.job.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 发件箱重推超时任务
 * 
 * @author Lenovo
 *
 */
@Slf4j
@Component
public class SortingSendBoxTask {

	@Autowired
	private ISendBoxService sendBoxService;

    @Autowired
    private ISendRecordService sendRecordService;

	public void run() 
	{
		String autoAgainSortingSendBoxSwitch = "OFF";
		Code autoAgainSortingSendBoxSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainSortingSendBoxSwitch");
	    if(autoAgainSortingSendBoxSwitchCode!=null&&!"".equals(autoAgainSortingSendBoxSwitchCode.getName()))
	    {
	    	autoAgainSortingSendBoxSwitch = autoAgainSortingSendBoxSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainSortingSendBoxSwitch))
	    {
	        SendBoxQuery querySendBox = new SendBoxQuery();
	        //分拣时间为空
	        querySendBox.setIsSortingFlag(false);
	        //定时时间为空
	        querySendBox.setIsTiming(false);
	        //审核状态通过
	        querySendBox.setAuditState(AuditStateEnums.Pass.getCode().intValue());
	        //有效短信数为0
	        querySendBox.setSmsCount(0);
	        
	        //获取当前时间
	        Date createTime = new Date();
	        //默认查询开始时间为：当前时间前6个小时（单位：分）
	        int sortingStartTimeInt = -60*6;
	        Code sortingStartTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sortingStartTimeInt");
	        if(sortingStartTimeIntCode!=null&&!"".equals(sortingStartTimeIntCode.getName()))
	        {
	        	sortingStartTimeInt = Integer.parseInt(sortingStartTimeIntCode.getName());
	        }
	        Date createStartTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingStartTimeInt);
	        querySendBox.setCreateStartTime(createStartTime);
	        
	        //默认查询结束时间为：当前时间前15分钟（单位：分）
	        int sortingEndTimeInt = -15;
	        Code sortingEndTimeIntCode = DatabaseCache.getCodeBySortCodeAndCode("System","sortingEndTimeInt");
	        if(sortingEndTimeIntCode!=null&&!"".equals(sortingEndTimeIntCode.getName()))
	        {
	        	sortingEndTimeInt = Integer.parseInt(sortingEndTimeIntCode.getName());
	        }
	        Date createEndTime = DateUtils.getDate(createTime, Calendar.MINUTE, sortingEndTimeInt);
	        querySendBox.setCreateEndTime(createEndTime);
	        
			loopQuery(querySendBox);
	    }
	    else
	    {
	    	log.info("SortingSendBoxTask任务开关未开启NO1");
	    }
	}
	
	private void loopQuery(SendBoxQuery querySendBox) 
	{
		String autoAgainSortingSendBoxSwitch = "OFF";
		Code autoAgainSortingSendBoxSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","autoAgainSortingSendBoxSwitch");
	    if(autoAgainSortingSendBoxSwitchCode!=null&&!"".equals(autoAgainSortingSendBoxSwitchCode.getName()))
	    {
	    	autoAgainSortingSendBoxSwitch = autoAgainSortingSendBoxSwitchCode.getName();
	    }
	    if("ON".equals(autoAgainSortingSendBoxSwitch))
	    {
	        //查询以上条件的发件箱记录（指定时间内createStartTime~createEndTime，已审核通过、非定时短信、未分拣的记录）
	    	List<SendBox> sendBoxList = sendBoxService.findSendBoxs(querySendBox);
        	if(sendBoxList!=null&&sendBoxList.size()>0)
			{
				List<SendBox> sortingSendBoxList = new ArrayList<SendBox>();
	    		for (int i = 0; i < sendBoxList.size(); i++) 
	        	{
	        		SendBox thisSendBox = sendBoxList.get(i);
	        		//以批次号与商户号为条件进行查询
	        		SendRecordQuery sendRecord = new SendRecordQuery();
	        		sendRecord.setSendCode(thisSendBox.getSendCode());
	        		sendRecord.setOrgCode(thisSendBox.getOrgCode());
	        		List<SendRecord> sendRecordList =  sendRecordService.findSendRecords(sendRecord);
	        		//判断发送记录表中没有分拣后的数据记录
	        		if(sendRecordList!=null&&sendRecordList.size()>0)
	        		{
	        			continue;
	        		}
	        		sortingSendBoxList.add(thisSendBox);
				}
	    		if(sortingSendBoxList!=null&&sortingSendBoxList.size()>0)
	    		{
	    			//推送进行分拣
	    			sendBoxService.splitRecord(sortingSendBoxList);
	    		}
	    		else
	    		{
	    			log.info("SortingSendBoxTask没有符合条件的记录数据NO2");
	    		}
			}
	    }
	    else
	    {
	    	log.info("SortingSendBoxTask任务开关未开启NO2");
	    }
	}
}
