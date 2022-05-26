package com.hero.sms.commands.sortingSMS;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.channel.SmsChannel;
import org.apache.commons.chain.Context;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 过滤可用通道
 *
 * 日发送量达到日限量（dayLimit）的通道过滤掉
 */
public class FilterChannelCommand extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        List<SmsChannel> channels = (List<SmsChannel>) context.get(LIST_SMS_CHANNEL);
        RedisHelper redisHelper = SpringContextUtil.getBean(RedisHelper.class);
        //过滤掉发送量已经达到日限量的通道
        List<SmsChannel> useableChannels = channels.stream()
                .filter(channel -> {
                    String key = channel.getId() + "_" + DateUtil.getString(new Date(), "yyyyMMdd");
                    Integer result = (Integer) redisHelper.get(key);
                    if (channel.getDayLimit() == null) return true; //日限量不设值  表示无限量 
                    return result == null || Long.valueOf(result) < channel.getDayLimit();
                }).collect(Collectors.toList());
        context.put(LIST_SMS_CHANNEL,useableChannels);
        return false;
    }
}
