package com.hero.sms.commands.pay;

import java.util.Date;

import org.apache.commons.chain.Context;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;

/**
 * 更新商户充值订单
 */
public class UpdateOrgRechargeOrderCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        OrganizationRechargeOrder order = (OrganizationRechargeOrder) context.get(RECHARGE_ORDER);
        order.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        order.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        order.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        return false;
    }
}
