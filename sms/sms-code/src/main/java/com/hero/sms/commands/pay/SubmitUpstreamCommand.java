package com.hero.sms.commands.pay;

import java.util.Date;

import org.apache.commons.chain.Context;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.channel.payTransfer.BasePayChannel;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;


/**
 * 提交上游
 */
public class SubmitUpstreamCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        PayChannel payChannel = (PayChannel) context.get(PAY_CHANNEL);
        OrganizationRechargeOrder rechargeOrder = (OrganizationRechargeOrder) context.get(RECHARGE_ORDER);
        IOrganizationRechargeOrderService orderService = (IOrganizationRechargeOrderService) context.get(RECHARGE_ORDER_SERVICE);
        BasePayChannel channel = (BasePayChannel) Class.forName(payChannel.getImplFullClass()).newInstance();
        FebsResponse response = channel.pay(payChannel,rechargeOrder);
        if(200 == (int) response.get("code")){
            context.put(PAY_URL,response.get(PAY_URL));
            context.put(PAY_URL_TYPE,response.get(PAY_URL_TYPE));
            rechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
            rechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
            orderService.updateById(rechargeOrder);
            return false;
        }
        rechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","失败").getCode());
        rechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        orderService.updateById(rechargeOrder);
        context.put(STR_ERROR_INFO,"请求失败");
        return true;
    }
}
