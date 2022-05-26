package com.hero.sms.commands.receiptReturnRecord;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.service.impl.channel.push.BaseSmsPushService;

import lombok.extern.slf4j.Slf4j;

/**
 * 初始化短信通道
 * @author Lenovo
 *
 */
@Slf4j
public class InitSmsChannelCommand extends BaseCommand {

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		SmsChannelExt smsChannelExt = (SmsChannelExt)context.get(OBJ_SMS_CHANNEL);
        try {
        	BaseSmsPushService pushService = (BaseSmsPushService) Class.forName(smsChannelExt.getImplFullClass()).newInstance();
            pushService.init(smsChannelExt);
            context.put(OBJ_PUSH_SERVICE, pushService);
        } catch (Exception e) {
        	String errInfo = String.format("回执处理，初始化通道【%s】失败",smsChannelExt.getCode());
        	context.put(STR_ERROR_INFO, errInfo);
        	log.error(errInfo,e);
        	return false;
        }
		return false;
	}

}
