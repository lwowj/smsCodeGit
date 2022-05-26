package com.hero.sms.commands.pay;

import java.util.Date;

import org.apache.commons.chain.Context;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import com.hero.sms.utils.RandomUtil;

/**
 *
 * 初始化商户充值订单信息
 *
 */
public class InitOrganizationRechargeOrderCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {

        OrganizationRechargeOrder rechargeOrder = new OrganizationRechargeOrder();
        PayChannel payChannel = (PayChannel) context.get(PAY_CHANNEL);
        Organization organization = (Organization) context.get(ORG);
        Integer amount = (Integer) context.get(AMOUNT);
        String netwayCode = (String) context.get(NETWAY_CODE);
        IOrganizationRechargeOrderService orderService = (IOrganizationRechargeOrderService) context.get(RECHARGE_ORDER_SERVICE);

        rechargeOrder.setNetwayCode(netwayCode);
        rechargeOrder.setOrganizationCode(organization.getOrganizationCode());
        rechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomUtil.randomStr(5));
        rechargeOrder.setPayChannelId(payChannel.getId() + "");
        rechargeOrder.setPayMerchNo(payChannel.getMerchNo());
        rechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","商户充值").getCode());
        rechargeOrder.setRechargeAmount(amount);
        rechargeOrder.setAvailableAmount(0);
        rechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","初始").getCode());
        rechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","初始").getCode());
        rechargeOrder.setCeateUser(organization.getOrganizationName());
        rechargeOrder.setCreateTime(new Date());
        orderService.save(rechargeOrder);

        context.put(RECHARGE_ORDER,rechargeOrder);

        return false;
    }
}
