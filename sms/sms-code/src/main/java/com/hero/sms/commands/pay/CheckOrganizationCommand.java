package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.organization.IOrganizationService;

/**
 * 校验商户信息
 */
public class CheckOrganizationCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        Organization organization = (Organization) context.get(ORG);
        IOrganizationService organizationService = (IOrganizationService) context.get(ORG_SERVICE);
        if(organization.getStatus().equals(OrgStatusEnums.Normal.getCode())
                && organization.getApproveStateCode().equals(OrgApproveStateEnums.SUCCESS.getCode())
                && organization.getDataMd5().equals(organizationService.getDataMd5(organization))){
            return false;
        }
        context.put(STR_ERROR_INFO,"商户信息异常！");
        return true;
    }
}
