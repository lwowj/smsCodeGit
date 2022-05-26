package com.hero.sms.service.impl.channel.payTransfer.transfer;

import com.hero.sms.commands.pay.PayBaseCommand;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.channel.payTransfer.BasePayChannel;

public class TestChannel implements BasePayChannel {

    @Override
    public FebsResponse pay(PayChannel payChannel, OrganizationRechargeOrder rechargeOrder) {

        return new FebsResponse().success().put(PayBaseCommand.PAY_URL,"https://www.baidu.com").put(PayBaseCommand.PAY_URL_TYPE,"scan");
    }

    @Override
    public FebsResponse payQuery(PayChannel payChannel,OrganizationRechargeOrder rechargeOrder) {
        return new FebsResponse().success();
    }

    @Override
    public FebsResponse payResult(PayChannel payChannel,OrganizationRechargeOrder rechargeOrder,String data) {
        return new FebsResponse().success();
    }
}
