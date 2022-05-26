package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.organization.IOrganizationService;

/**
 * 更新商户信息
 */
public class UpdateOrganizationCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        Organization organization = (Organization) context.get(ORG);
        OrganizationRechargeOrder order = (OrganizationRechargeOrder) context.get(RECHARGE_ORDER);
        IOrganizationService organizationService = (IOrganizationService) context.get(ORG_SERVICE);
        organization.setAmount(organization.getAmount() + order.getRechargeAmount());
        organization.setAvailableAmount(organization.getAvailableAmount() + order.getRechargeAmount());
        organization.setDataMd5(organizationService.getDataMd5(organization));
        order.setAvailableAmount(new Long(organization.getAvailableAmount()).intValue());
        return false;
    }
}
