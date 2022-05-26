package com.hero.sms.commands.common;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import org.apache.commons.chain.Context;

/**
 * 校验商户与代理资费配置（针对指定运营商）
 *
 */
public class CheckOrgAgentChargesCommand extends BaseCommand{
		
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {

    	BaseSend baseSend = (BaseSend) context.get(OBJ_BASESEND_ENTITY);
		OrganizationCost organizationCost =  (OrganizationCost) context.get(OBJ_ORG_COST);
		int orgCost = Integer.parseInt(organizationCost.getCostValue());
		Integer agentCost = (Integer)context.get(INT_AGENT_COST);
		Integer upAgentCost = (Integer)context.get(INT_UPAGENT_COST);

		if(orgCost < agentCost.intValue())
		{
			context.put(STR_ERROR_INFO, "商户资费配置错误");
			return true;
		}
		if(upAgentCost != null)
		{
			if(agentCost.intValue() < upAgentCost.intValue())
			{
				context.put(STR_ERROR_INFO, "代理资费配置错误");
				return true;
			}
		}

        String smsNumberArea = baseSend.getSmsNumberArea();
		if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
		{
			OrganizationCost organizationCostCtcc =  (OrganizationCost) context.get(OBJ_ORG_COST_CTCC);
			int orgCostCtcc = Integer.parseInt(organizationCostCtcc.getCostValue());
			Integer agentCostCtcc = (Integer)context.get(INT_AGENT_COST_CTCC);
			Integer upAgentCostCtcc = (Integer)context.get(INT_UPAGENT_COST_CTCC);
			if(orgCostCtcc < agentCostCtcc.intValue())
			{
				context.put(STR_ERROR_INFO, "商户资费配置错误");
				return true;
			}
			if(upAgentCostCtcc != null)
			{
				if(agentCostCtcc.intValue() < upAgentCostCtcc.intValue())
				{
					context.put(STR_ERROR_INFO, "代理资费配置错误");
					return true;
				}
			}

			OrganizationCost organizationCostCucc =  (OrganizationCost) context.get(OBJ_ORG_COST_CUCC);
			int orgCostCucc = Integer.parseInt(organizationCostCucc.getCostValue());
			Integer agentCostCucc = (Integer)context.get(INT_AGENT_COST_CUCC);
			Integer upAgentCostCucc = (Integer)context.get(INT_UPAGENT_COST_CUCC);
			if(orgCostCucc < agentCostCucc.intValue())
			{
				context.put(STR_ERROR_INFO, "商户资费配置错误");
				return true;
			}
			if(upAgentCostCucc != null)
			{
				if(agentCostCucc.intValue() < upAgentCostCucc.intValue())
				{
					context.put(STR_ERROR_INFO, "代理资费配置错误");
					return true;
				}
			}

			OrganizationCost organizationCostCmCC =  (OrganizationCost) context.get(OBJ_ORG_COST_CMCC);
			int orgCostCmcc = Integer.parseInt(organizationCostCmCC.getCostValue());
			Integer agentCostCmcc = (Integer)context.get(INT_AGENT_COST_CMCC);
			Integer upAgentCostCmcc = (Integer)context.get(INT_UPAGENT_COST_CMCC);
			if(orgCostCmcc < agentCostCmcc.intValue())
			{
				context.put(STR_ERROR_INFO, "商户资费配置错误");
				return true;
			}
			if(upAgentCostCmcc != null)
			{
				if(agentCostCmcc.intValue() < upAgentCostCmcc.intValue())
				{
					context.put(STR_ERROR_INFO, "代理资费配置错误");
					return true;
				}
			}
		}
        return false;
    }
}
