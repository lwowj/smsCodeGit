package com.hero.sms.commands.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgCostStateEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.organization.IOrganizationService;

/**
 * 查询商户状态
 *
 */
public class QueryOrgCommand extends BaseCommand{
		
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {

    	BaseSend baseSend = (BaseSend) context.get(OBJ_BASESEND_ENTITY);
        IOrganizationService organizationService = (IOrganizationService) context.get(OBJ_ORG_SERVICE);

        OrganizationExt org = organizationService.queryOrganizationExtByOrgCode(baseSend.getOrgCode());
        context.put(OBJ_QUERY_ORG, org);
        if (org == null){
            context.put(STR_ERROR_INFO, "商户不存在！");
            return true;
        }
        if (!OrgStatusEnums.Normal.getCode().equals(org.getStatus())){
            context.put(STR_ERROR_INFO, "商户状态被锁！");
            return true;
        }
        if (!OrgApproveStateEnums.SUCCESS.getCode().equals(org.getApproveStateCode())){
            context.put(STR_ERROR_INFO, "商户未审核！");
            return true;
        }

        OrganizationCost organizationCost = org.getOrganizationCost(baseSend.getSmsType(), baseSend.getSmsNumberArea());
        if(organizationCost == null) {
        	context.put(STR_ERROR_INFO, "商户资费未配置");
        	return true;
        }
        String state = organizationCost.getState();
        if(!state.equals(OrgCostStateEnums.Normal.getCode())) {
        	context.put(STR_ERROR_INFO, "商户资费未开启");
        	return true;
        }
        if(StringUtils.isBlank(organizationCost.getCostValue()) || Integer.parseInt(organizationCost.getCostValue()) <= 0 ) {
        	context.put(STR_ERROR_INFO, "商户资费配置错误");
        	return true;
        }
        String channelId = organizationCost.getChannelId();
        if(StringUtils.isNotBlank(channelId)) {
        	context.put(STR_ASSIGN_CHANNEL_ID, channelId);
        }
        context.put(OBJ_ORG_COST, organizationCost);
        /**
         * @begin 2021-11-13
         * 新增保存运营商资费
         */
        String smsNumberArea = baseSend.getSmsNumberArea();
		if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
		{
			/**
			 * 返回运营商字段不会空的配置列表
			 */
			List<OrganizationCost> organizationCostList =  org.getOrganizationCostList(baseSend.getSmsType(), baseSend.getSmsNumberArea());
			if(organizationCostList!=null && organizationCostList.size()>0)
			{
				Map<String, OrganizationCost> orgCostMap = new HashMap<String, OrganizationCost>();
				for (int i = 0; i < organizationCostList.size(); i++) 
				{
					OrganizationCost orgCost = organizationCostList.get(i);
					String thisState = orgCost.getState();
					String thisCostValue = orgCost.getCostValue();
					String thisOperator = orgCost.getOperator();
					if(!thisState.equals(OrgCostStateEnums.Normal.getCode()) || StringUtils.isBlank(thisCostValue) || Integer.parseInt(thisCostValue) <= 0 ) 
				    {
						continue;
				    }

			    	orgCostMap.put(thisOperator, orgCost);
				}
				
				if(orgCostMap.get(OperatorEnums.CTCC.getCode()) != null)
				{
					context.put(OBJ_ORG_COST_CTCC, orgCostMap.get(OperatorEnums.CTCC.getCode()));
				}
				else
				{
					context.put(OBJ_ORG_COST_CTCC, organizationCost);
				}
				
				if(orgCostMap.get(OperatorEnums.CUCC.getCode()) != null)
				{
					context.put(OBJ_ORG_COST_CUCC, orgCostMap.get(OperatorEnums.CUCC.getCode()));
				}
				else
				{
					context.put(OBJ_ORG_COST_CUCC, organizationCost);
				}
				
				if(orgCostMap.get(OperatorEnums.CMCC.getCode()) != null)
				{
					context.put(OBJ_ORG_COST_CMCC, orgCostMap.get(OperatorEnums.CMCC.getCode()));
				}
				else
				{
					context.put(OBJ_ORG_COST_CMCC, organizationCost);
				}
			}
			else
			{
				context.put(OBJ_ORG_COST_CTCC, organizationCost);
				context.put(OBJ_ORG_COST_CUCC, organizationCost);
				context.put(OBJ_ORG_COST_CMCC, organizationCost);
			}
		}
		/**
         * @end
         */
        context.put(OBJ_ORG_AVAILABLE_AMOUNT, org.getAvailableAmount());
        return false;
    }
}
