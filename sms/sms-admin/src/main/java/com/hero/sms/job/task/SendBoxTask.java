package com.hero.sms.job.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.service.message.ISendBoxService;

import lombok.extern.slf4j.Slf4j;

/**
 * 发件箱定时任务
 * 
 * @author Lenovo
 *
 */
@Slf4j
@Component
public class SendBoxTask {

	@Autowired
	private ISendBoxService sendBoxService;


	public void run() {

		SendBoxQuery querySendBox = new SendBoxQuery();
		querySendBox.setCreateStartTime(DateUtil.add(new Date(), Calendar.HOUR,-73));
		querySendBox.setLeTimingTime(new Date());
		querySendBox.setAuditState(AuditStateEnums.Pass.getCode());
		querySendBox.setIsNeedSmsNumber(true);
		
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setPageSize(500);
		loopQuery(queryRequest,querySendBox);
	}
	
	private void loopQuery(QueryRequest queryRequest,SendBoxQuery querySendBox) {
		IPage<SendBox> sendBoxs = sendBoxService.findSendBoxs(queryRequest, querySendBox);
		if (sendBoxs != null && CollectionUtils.isNotEmpty(sendBoxs.getRecords())) {
			List<SendBox> records = sendBoxs.getRecords();
			batchClearTimingTime(records);
			sendBoxService.splitRecord(records);
			long size = sendBoxs.getPages();
			if (size > 1){
				loopQuery(queryRequest,querySendBox);
			}
		}
	}

	private void batchClearTimingTime(List<SendBox> sendBoxes){

		List<Long> ids = sendBoxes.stream().map(BaseSend::getId).collect(Collectors.toList());
		LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(SendBox::getTimingTime,null)
				.in(SendBox::getId,ids);
		sendBoxService.update(updateWrapper);
	}
}
