package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CalculationAmountCommand extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        SendRecord sendRecord = (SendRecord) context.get(OBJ_SAVE_SENDRECORD_ENTITY);

        OrganizationCost orgCostObj = (OrganizationCost) context.get(OBJ_ORG_COST);
        int orgCost = Integer.parseInt(orgCostObj.getCostValue());
        Integer agentCost = (Integer)context.get(INT_AGENT_COST);
        Integer upAgentCost = (Integer)context.get(INT_UPAGENT_COST);
        Integer channelCost = (Integer)context.get(INT_SMS_CHANNEL_COST);

        Integer smsCount = sendRecord.getSmsCount();
        
        /**
         * @begin 2021-11-09
         * 根据运营商获取资费
         */
    	String smsNumberArea = sendRecord.getSmsNumberArea();
		if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
		{
	        String operator = sendRecord.getSmsNumberOperator();
	        if(StringUtil.isNotBlank(operator))
	        {
	        	try 
	        	{
	        		if(operator.equals(OperatorEnums.CTCC.getCode()))
		        	{
	        			if(context.get(OBJ_ORG_COST_CTCC) != null)
	        			{
	        				OrganizationCost orgCostObjCtcc = (OrganizationCost) context.get(OBJ_ORG_COST_CTCC);
			                orgCost = Integer.parseInt(orgCostObjCtcc.getCostValue());
	        			}
	        			if(context.get(INT_AGENT_COST_CTCC) != null)
	        			{
	        				agentCost = (Integer)context.get(INT_AGENT_COST_CTCC);
	        			}
	        			if(context.get(INT_UPAGENT_COST_CTCC) != null)
	        			{
	        				 upAgentCost = (Integer)context.get(INT_UPAGENT_COST_CTCC);
	        			}
		        	}
		        	else if(operator.equals(OperatorEnums.CUCC.getCode()))
		        	{
		        		if(context.get(OBJ_ORG_COST_CUCC) != null)
	        			{
		        			OrganizationCost orgCostObjCucc = (OrganizationCost) context.get(OBJ_ORG_COST_CUCC);
			                orgCost = Integer.parseInt(orgCostObjCucc.getCostValue());
	        			}
	        			if(context.get(INT_AGENT_COST_CUCC) != null)
	        			{
	        				agentCost = (Integer)context.get(INT_AGENT_COST_CUCC);
	        			}
	        			if(context.get(INT_UPAGENT_COST_CUCC) != null)
	        			{
	        				upAgentCost = (Integer)context.get(INT_UPAGENT_COST_CUCC);
	        			}
		        	}
		        	else if(operator.equals(OperatorEnums.CMCC.getCode()))
		        	{
		        		if(context.get(OBJ_ORG_COST_CMCC) != null)
	        			{
		        			OrganizationCost orgCostObjCmcc = (OrganizationCost) context.get(OBJ_ORG_COST_CMCC);
			                orgCost = Integer.parseInt(orgCostObjCmcc.getCostValue());
	        			}
	        			if(context.get(INT_AGENT_COST_CMCC) != null)
	        			{
	        				agentCost = (Integer)context.get(INT_AGENT_COST_CMCC);
	        			}
	        			if(context.get(INT_UPAGENT_COST_CMCC) != null)
	        			{
	        				upAgentCost = (Integer)context.get(INT_UPAGENT_COST_CMCC);
	        			}
		        	}
				} catch (Exception e) {
					// TODO: handle exception
				} 
	        }
		}
		/**
         * @end
         */
		
        //商户消费
        Integer orgCashAmout = smsCount * orgCost;
        sendRecord.setConsumeAmount(orgCashAmout);


        //通道成本
        Integer channelAmout = smsCount * channelCost;
        sendRecord.setChannelCostAmount(channelAmout);

//        /**
//         * @begin 2021-10-18
//         * 新增非国内号段校验，将资费ID存入SmsNumberProvince字段
//         */
//        try {
//        	String smsNumberArea = sendRecord.getSmsNumberArea();
//        	if(!smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
//        	{
//        		Integer channelCostId = (Integer)context.get(INT_SMS_CHANNEL_COST_ID);
//        		sendRecord.setSmsNumberProvince(String.valueOf(channelCostId));
//        	}
//        	
//		} catch (Exception e) {}
//        /**
//         * @end
//         */
        
        //代理收益
        Integer agentIncomeAmount = 0;
        if (agentCost != null){
			agentIncomeAmount = smsCount * (orgCost - agentCost) ;
        }
        sendRecord.setAgentIncomeAmount(agentIncomeAmount);
        
        //上级代理收益
        int upAgentIncomeAmount = 0;
        if(upAgentCost != null) {
        	upAgentIncomeAmount = smsCount * (agentCost - upAgentCost);
        }
        sendRecord.setUpAgentIncomeAmount(upAgentIncomeAmount);

        //平台收益
        Integer incomeAmount = orgCashAmout - channelAmout - agentIncomeAmount - upAgentIncomeAmount;
        sendRecord.setIncomeAmount(incomeAmount);
        return false;
    }
}
