package com.hero.sms.commands.receiptReturnRecord;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.service.channel.ISmsChannelService;

/**
 * 获取短信通道
 * @author Lenovo
 *
 */
public class QuerySmsChannelCommand extends BaseCommand {

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		ISmsChannelService smsChannelService = (ISmsChannelService)context.get(OBJ_CHANNEL_SERVICE);
		SmsChannelExt smsChannelExt = null;
		Integer channelId = (Integer)context.get(INT_CHANNEL_ID);
		if(channelId != null) {
			smsChannelExt = smsChannelService.findContainPropertyById(channelId);
			if(smsChannelExt == null) {
				context.put(STR_ERROR_INFO, String.format("短信通道【%s】不存在",channelId));
				return true;
			}
		}else {
			String channelCode = (String)context.get(STR_CHANNEL_CODE);
			if(StringUtils.isNotBlank(channelCode)) {
				smsChannelExt = smsChannelService.findContainPropertyByCode(channelCode);
			}
			if(smsChannelExt == null) {
				context.put(STR_ERROR_INFO, String.format("短信通道【%s】不存在",channelCode));
				return true;
			}
		}
		context.put(OBJ_SMS_CHANNEL, smsChannelExt);
		return false;
	}

}
