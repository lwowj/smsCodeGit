package com.hero.sms.commands.sortingSMS;

import java.util.Collection;
import java.util.List;

import org.apache.commons.chain.Context;

import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.service.message.ISendRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 批量保存发送记录
 * @author Lenovo
 *
 */
@Slf4j
public class BatchSaveSendRecordCommand  extends BaseCommand {
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		ISendRecordService sendRecordService = (ISendRecordService) context.get(OBJ_SENDRECORD_SERVICE);
		List<SendRecord> sendRecords = (List<SendRecord>) context.get(LIST_SEND_RECORD);
		
		int sendRecordSize = sendRecords.size();
		if(sendRecordSize > 0) {
			Collection<SendRecord> temp = Lists.newArrayList();
			for (int i = 0 ; i < sendRecordSize; i++) {
				temp.add(sendRecords.get(i));
				if( i == (sendRecordSize-1) || (i+1)%1000==0) {
					sendRecordService.saveBatch(temp);
					temp.clear();
				}
			}
		}
		return false;
	}

}
