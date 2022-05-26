package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.message.SendRecord;

/**
 * 分拣成功，顶级代理 日发送量累加
 */
public class IncreaseTopAgentSend extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        SendRecord sendRecord = (SendRecord) context.get(BaseCommand.OBJ_SAVE_SENDRECORD_ENTITY);
        String key = (String) context.get(TOPAGENT_DAY_INC_KEY);
        long incrNum = 1L;
        if (sendRecord.getSmsCount() != null){
            incrNum = sendRecord.getSmsCount();
        }
        //顶级代理日发送量累加
        RedisHelper redisHelper = SpringContextUtil.getBean(RedisHelper.class);
        redisHelper.incr(key,incrNum);
        return false;
    }
}
