package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.channel.IPayChannelService;
import com.hero.sms.service.channel.payTransfer.BasePayChannel;

/**
 * 校验回调结果
 */
public class CheckResultCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        JSONObject data = (JSONObject) context.get(PAY_DATA);
        OrganizationRechargeOrder order = (OrganizationRechargeOrder) context.get(RECHARGE_ORDER);
        IPayChannelService payChannelService = (IPayChannelService) context.get(PAY_CHANNEL_SERVICE);
        PayChannel payChannel = payChannelService.getById(order.getPayChannelId());
        if(payChannel != null){
            BasePayChannel channel = (BasePayChannel) Class.forName(payChannel.getImplFullClass()).newInstance();
            FebsResponse response = channel.payResult(payChannel,order,data.getString("data"));
            if(200 == (int) response.get("code")){
                return false;
            }
        }
        return true;
    }
}
