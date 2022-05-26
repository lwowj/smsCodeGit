package com.hero.sms.commands.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.service.channel.ISmsChannelService;

/**
 * 查询可用通道
 *
 */
public class QueryUseableSmsChannelCommand extends BaseCommand{
		
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {

    	ISmsChannelService smsChannelService = (ISmsChannelService) context.get(OBJ_CHANNEL_SERVICE);
    	String assignChannelId = (String)context.get(STR_ASSIGN_CHANNEL_ID);
    	if(StringUtils.isNotBlank(assignChannelId)) {
    		SmsChannel smsChannel = smsChannelService.getById(assignChannelId);
    		if(smsChannel == null) {
    			context.put(STR_ERROR_INFO, "指定通道不存在");
        		return true;
    		}
    		if(smsChannel.getState().intValue() != ChannelStateEnums.START.getCode() && smsChannel.getState().intValue() != ChannelStateEnums.Pause.getCode()) {
    			context.put(STR_ERROR_INFO, "指定通道未开启");
        		return true;
    		}
    		List<SmsChannel> channels = Lists.newArrayList(smsChannel);
    		context.put(LIST_SMS_CHANNEL, channels);
    		return false;
    	}
    	
    	BaseSend baseSend = (BaseSend) context.get(OBJ_BASESEND_ENTITY);
        SmsChannel querySmsChannel = new SmsChannel();
    	querySmsChannel.setState(ChannelStateEnums.START.getCode());
    	querySmsChannel.setSupportArea(baseSend.getSmsNumberArea());
    	/**
    	 * 修改成根据资费配置查询出对应通道
    	 * 2022-03-22
    	 */
    	List<SmsChannel> channels = new ArrayList<SmsChannel>();
    	if(baseSend.getSmsNumberArea().equals(SmsNumberAreaCodeEnums.China.getInArea()))
    	{
    		channels = smsChannelService.findSmsChannels(querySmsChannel);
    	}
    	else
    	{
    		channels = smsChannelService.findSmsChannelsForArea(querySmsChannel);
    	}
    	 
    	if(CollectionUtils.isEmpty(channels)) {
    		context.put(STR_ERROR_INFO, "当前没有可用的短信通道");
    		return true;
    	}
        context.put(LIST_SMS_CHANNEL, channels);
        return false;
    }
}
