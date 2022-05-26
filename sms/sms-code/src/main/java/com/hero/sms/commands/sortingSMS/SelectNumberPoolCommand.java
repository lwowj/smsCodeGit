package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.service.channel.INumberPoolService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectNumberPoolCommand extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        INumberPoolService numberPoolService= SpringContextUtil.getBean(INumberPoolService.class);
        SendRecord sendRecord = (SendRecord) context.get(OBJ_SAVE_SENDRECORD_ENTITY);
        SmsChannel smsChannel = (SmsChannel) context.get(OBJ_SMS_CHANNEL);

        if (smsChannel == null){
            log.warn("【号码池选择命令】通道为空，跳出命令执行!");
            return false;
        }
        String number = numberPoolService.randomNumberPoolBySmsChannelId(smsChannel.getId());
        if (StringUtils.isNotBlank(number)){
            sendRecord.setSourceNumber(number);
        }
        return false;
    }
}
