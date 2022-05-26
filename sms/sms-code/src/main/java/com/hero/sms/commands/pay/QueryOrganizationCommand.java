package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.service.organization.IOrganizationService;

/**
 * 查询充值商户
 */
public class QueryOrganizationCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        JSONObject data = (JSONObject) context.get(PAY_DATA);
        String orgCode = data.getString("organizationCode");
        IOrganizationService organizationService = (IOrganizationService) context.get(ORG_SERVICE);
        Organization organization = organizationService.queryOrgByCodeForUpdate(orgCode);
        if(organization != null){
            context.put(ORG,organization);
            return false;
        }
        context.put(STR_ERROR_INFO,"商户不存在！");
        return true;
    }
}
