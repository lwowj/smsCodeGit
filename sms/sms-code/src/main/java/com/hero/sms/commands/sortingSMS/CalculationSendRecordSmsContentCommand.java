package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.utils.sensi.SensitiveFilter;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendRecord;

import lombok.extern.slf4j.Slf4j;

/**
 * 计算短信内容长度
 * @author Lenovo
 *
 */
public class CalculationSendRecordSmsContentCommand extends BaseCommand{

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		SendBox sendBox = (SendBox)context.get(OBJ_SENDBOX_ENTITY);
		SendRecord sendRecord = (SendRecord)context.get(OBJ_SAVE_SENDRECORD_ENTITY);
		int words = 70;
/*		Code wordsCode = DatabaseCache.getCodeBySortCodeAndCode("System", "wordsOfPerMsg");
		if (wordsCode != null) {
			words = Integer.parseInt(wordsCode.getName());
		}*/
		String content = sendRecord.getSmsContent();
		int smsWords = content.length();
		if (smsWords > 70){//超过70字拆分长短信，按67一个片段拆分
			words = 67;
		}
		sendRecord.setSmsWords(smsWords);
		sendBox.setSmsWords(smsWords);
		int smsCount = (int) Math.ceil(smsWords * 1d / words);
		sendRecord.setSmsCount(smsCount);
		/**
		 * @begin
		 * 判断内容敏感词
		 * 2022-02-17
		 */
		try
		{
			SensitiveFilter sensitiveFilter = SensitiveFilter.DEFAULT;
			boolean isContainSens = sensitiveFilter.isContain(content);
			if(isContainSens)
			{
				String msg = "内容包含敏感词";
//				String msg = String.format("内容包含敏感词【%s】",sensitiveFilter.filter(content, '*'));
				context.put(STR_ERROR_INFO, msg);
				context.put(STR_NOTIFY_MSG, msg);
				return true;
			}
		} catch (Exception e) {}
		/**
		 * @end
		 */
		return false;
	}

}
