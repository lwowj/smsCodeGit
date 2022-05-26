package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;

/**
 * 更新提交
 */
public class UpdateSubmitCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        Organization organization = (Organization) context.get(ORG);
        OrganizationRechargeOrder order = (OrganizationRechargeOrder) context.get(RECHARGE_ORDER);
        IOrganizationService organizationService = (IOrganizationService) context.get(ORG_SERVICE);
        IOrganizationRechargeOrderService orderService = (IOrganizationRechargeOrderService) context.get(RECHARGE_ORDER_SERVICE);
        orderService.updateById(order);
        organizationService.updateById(organization);
        return false;
    }
}
