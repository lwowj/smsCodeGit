package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;

/**
 * 查询商户充值订单
 */
public class QueryOrgRechargeOrderCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        JSONObject data = (JSONObject) context.get(PAY_DATA);
        IOrganizationRechargeOrderService orderService = (IOrganizationRechargeOrderService) context.get(RECHARGE_ORDER_SERVICE);
        String orderNo = data.getString("orderNo");
        LambdaQueryWrapper<OrganizationRechargeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationRechargeOrder::getOrderNo,orderNo);
        wrapper.eq(OrganizationRechargeOrder::getRechargeType, DatabaseCache.getCodeBySortCodeAndName("RechargeType","商户充值").getCode());
        wrapper.eq(OrganizationRechargeOrder::getReqStateCode,DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        wrapper.eq(OrganizationRechargeOrder::getRechargeStateCode,DatabaseCache.getCodeBySortCodeAndName("PayStateType","初始").getCode());
        wrapper.last("FOR UPDATE");
        OrganizationRechargeOrder order = orderService.getOne(wrapper);
        if(order != null){
            data.put("organizationCode",order.getOrganizationCode());
            context.put(RECHARGE_ORDER,order);
            return false;
        }
        context.put(STR_ERROR_INFO,"未找到订单");
        return true;
    }
}
