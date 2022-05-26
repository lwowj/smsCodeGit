package com.hero.sms.commands.common;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.enums.organization.OrgCostStateEnums;

/**
 * 校验本批次号码数量下商户的余额是否充足
 *
 */
public class QueryOrgBalanceCommand extends BaseCommand{
		
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {

    	OrganizationExt org = (OrganizationExt) context.get(OBJ_QUERY_ORG);
    	SendBox sendBox = (SendBox)context.get(OBJ_BASESEND_ENTITY);
//    	 后付费用户  直接返回true
//        if(OrgSettlementTypeEnums.UsedPayment.getCode().equals(org.getSettlementType())) {
//        	context.put(STR_ERROR_INFO, "商户资费未配置");
//            return true;
//        }
        OrganizationCost organizationCost = (OrganizationCost)context.get(OBJ_ORG_COST);
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

        int smsCount =  sendBox.getNumberCount();
        int orgCost = Integer.parseInt(organizationCost.getCostValue());
        //消费金额
        Integer consumeAmount = smsCount * orgCost;
        if(consumeAmount.intValue() > org.getAvailableAmount().intValue()) 
        {
        	context.put(STR_ERROR_INFO, "商户余额不足（预计消费金额超过余额）");
            //商户余额不足
            return true;
        }
        return false;
    }
}
