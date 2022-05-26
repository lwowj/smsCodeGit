package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.entity.organization.Organization;

/**
 * 交易商户IP
 */
public class CheckOrganizationIPCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        Organization organization = (Organization) context.get(ORG);
        if(StringUtils.isNotEmpty(organization.getBindIp())){
            JSONObject data = (JSONObject) context.get(PAY_DATA);
            String ip = data.getString("ip");
            String[] bindIps = organization.getBindIp().split(",");
            boolean conform = false;
            for (String bindIp: bindIps) {
                if(bindIp.equals(ip)){
                    conform = true;
                    break;
                }
            }
            if(conform){
                return false;
            }
            context.put(STR_ERROR_INFO,"当前IP不能充值！");
            return true;
        }
        return false;
    }
}
