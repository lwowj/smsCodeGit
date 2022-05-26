package com.hero.sms.service.channel.payTransfer;

import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;

/**
 * 基础交易通道
 */
public interface BasePayChannel {

    FebsResponse febsResponse = new FebsResponse();

    FebsResponse pay(PayChannel payChannel, OrganizationRechargeOrder rechargeOrder);

    FebsResponse payQuery(PayChannel payChannel,OrganizationRechargeOrder rechargeOrder);

    FebsResponse payResult(PayChannel payChannel,OrganizationRechargeOrder rechargeOrder,String data);

}
