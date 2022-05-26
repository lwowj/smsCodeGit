package com.hero.sms.commands.pay;

import java.util.List;

import org.apache.commons.chain.Context;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.service.channel.IPayChannelService;


/**
 * 查询充值通道
 */
public class QueryPayChannelCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        String netwayCode = (String) context.get(NETWAY_CODE);
        IPayChannelService payChannelService = (IPayChannelService) context.get(PAY_CHANNEL_SERVICE);
        Integer amount = (Integer) context.get(AMOUNT);
        LambdaQueryWrapper<PayChannel> wrapper = new LambdaQueryWrapper();
        wrapper.eq(PayChannel::getNetwayCode,netwayCode);
        wrapper.eq(PayChannel::getStateCode, ChannelStateEnums.START.getCode());
        wrapper.le(PayChannel::getMinAmount,amount);
        wrapper.ge(PayChannel::getMaxAmount,amount);
        List<PayChannel> channels = payChannelService.list(wrapper);
        if(channels.size() > 0){
            context.put(PAY_CHANNEL_LIST,channels);
            return false;
        }
        context.put(STR_ERROR_INFO,"暂无通道");
        return true;
    }
}
