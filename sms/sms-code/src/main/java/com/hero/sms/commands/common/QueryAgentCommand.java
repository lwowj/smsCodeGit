package com.hero.sms.commands.common;

import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.entity.agent.ext.AgentExt;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.service.agent.IAgentService;

public class QueryAgentCommand extends BaseCommand {

    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
    	BaseSend baseSend = (BaseSend) context.get(OBJ_BASESEND_ENTITY);
        IAgentService agentService = (IAgentService) context.get(OBJ_AGENT_SERVICE);

        if(baseSend.getAgentId() == null) {
        	return false;
        }
        
        AgentExt agent = agentService.queryAgentExtById(baseSend.getAgentId());

        if (agent == null){
        	context.put(STR_ERROR_INFO, "代理不存在");
        	return true;
        }
        AgentCost agentCost = agent.getAgentCost(baseSend.getSmsType(), baseSend.getSmsNumberArea());
        if(agentCost == null) {
        	context.put(STR_ERROR_INFO, "代理资费未配置");
        	return true;
        }
        if(StringUtils.isBlank(agentCost.getValue()) || Integer.parseInt(agentCost.getValue()) <= 0 ) {
        	context.put(STR_ERROR_INFO, "代理资费配置错误");
        	return true;
        }
        context.put(INT_AGENT_COST, Integer.parseInt(agentCost.getValue()));
        context.put(OBJ_QUERY_AGENT, agent);
        String smsNumberArea = baseSend.getSmsNumberArea();
        if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
      	{
        	/**
             * @begin 2021-11-13
             * 新增保存代理运营商资费
             */
        	List<AgentCost> agentCostList = agent.getAgentCostList(baseSend.getSmsType(), baseSend.getSmsNumberArea());
        	if(agentCostList!=null && agentCostList.size()>0)
        	{
        		int ctccInt = -1;
        		int cuccInt = -1;
        		int cmccInt = -1;
        		for (int i = 0; i < agentCostList.size(); i++) 
        		{
        			AgentCost thisAgentCost = agentCostList.get(i);
					String thisCostValue = thisAgentCost.getValue();
					String thisOperator = thisAgentCost.getOperator();
					if(StringUtils.isBlank(thisCostValue) || Integer.parseInt(thisCostValue) <= 0 ) 
					{
						continue;
					}
					if(thisOperator.equals(OperatorEnums.CTCC.getCode()))
			    	{
						ctccInt = Integer.parseInt(thisAgentCost.getValue());
			    	}
			    	if(thisOperator.equals(OperatorEnums.CUCC.getCode()))
			    	{
			    		cuccInt = Integer.parseInt(thisAgentCost.getValue());
			    	}
			    	if(thisOperator.equals(OperatorEnums.CMCC.getCode()))
			    	{
			    		cmccInt = Integer.parseInt(thisAgentCost.getValue());
			    	}
        		}
        		if(ctccInt == -1)
				{
        			ctccInt = Integer.parseInt(agentCost.getValue());
				}
				if(cuccInt == -1)
				{
					cuccInt = Integer.parseInt(agentCost.getValue());
				}
				if(cmccInt == -1)
				{
					cmccInt = Integer.parseInt(agentCost.getValue());
				}
				context.put(INT_AGENT_COST_CTCC, ctccInt);
				context.put(INT_AGENT_COST_CUCC, cuccInt);
				context.put(INT_AGENT_COST_CMCC, cmccInt);
        	}
        	else
        	{
        		context.put(INT_AGENT_COST_CTCC,  Integer.parseInt(agentCost.getValue()));
        		context.put(INT_AGENT_COST_CUCC,  Integer.parseInt(agentCost.getValue()));
        		context.put(INT_AGENT_COST_CMCC,  Integer.parseInt(agentCost.getValue()));
        	}
            /**
             * @end
             */
      	}
        if(agent.getUpAgentId() == null) {
        	return false;
        }
        
        AgentExt upAgent = agentService.queryAgentExtById(agent.getUpAgentId());
        if(upAgent == null) {
        	context.put(STR_ERROR_INFO, "上级代理不存在");
        	return true;
        }
        
        AgentCost upAgentCost = upAgent.getAgentCost(baseSend.getSmsType(), baseSend.getSmsNumberArea());
        if(upAgentCost == null) {
        	context.put(STR_ERROR_INFO, "上级代理资费未配置");
        	return true;
        }
        if(StringUtils.isBlank(upAgentCost.getValue()) || Integer.parseInt(upAgentCost.getValue()) <= 0 ) {
        	context.put(STR_ERROR_INFO, "上级代理资费配置错误");
        	return true;
        }
        context.put(INT_UPAGENT_COST, Integer.parseInt(upAgentCost.getValue()));
        context.put(OBJ_QUERY_UPAGENT, upAgent);
        
        
        if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
      	{
            /**
             * @begin 2021-11-13
             * 新增保存上级运营商资费
             */
        	List<AgentCost> upAgentCostList = upAgent.getAgentCostList(baseSend.getSmsType(), baseSend.getSmsNumberArea());
        	if(upAgentCostList!=null && upAgentCostList.size()>0)
        	{
        		int upCtccInt = -1;
        		int upCuccInt = -1;
        		int upCmccInt = -1;
        		for (int i = 0; i < upAgentCostList.size(); i++) 
        		{
        			AgentCost thisUpAgentCost = upAgentCostList.get(i);
					String thisUpCostValue = thisUpAgentCost.getValue();
					String thisUpOperator = thisUpAgentCost.getOperator();
					if(StringUtils.isBlank(thisUpCostValue) || Integer.parseInt(thisUpCostValue) <= 0 ) 
					{
						continue;
					}
					if(thisUpOperator.equals(OperatorEnums.CTCC.getCode()))
			    	{
						upCtccInt = Integer.parseInt(thisUpAgentCost.getValue());
			    	}
			    	if(thisUpOperator.equals(OperatorEnums.CUCC.getCode()))
			    	{
			    		upCuccInt = Integer.parseInt(thisUpAgentCost.getValue());
			    	}
			    	if(thisUpOperator.equals(OperatorEnums.CMCC.getCode()))
			    	{
			    		upCmccInt = Integer.parseInt(thisUpAgentCost.getValue());
			    	}
        		}
        		if(upCtccInt == -1)
				{
        			upCtccInt = Integer.parseInt(upAgentCost.getValue());
				}
				if(upCuccInt == -1)
				{
					upCuccInt = Integer.parseInt(upAgentCost.getValue());
				}
				if(upCmccInt == -1)
				{
					upCmccInt = Integer.parseInt(upAgentCost.getValue());
				}
				context.put(INT_UPAGENT_COST_CTCC, upCtccInt);
				context.put(INT_UPAGENT_COST_CUCC, upCuccInt);
				context.put(INT_UPAGENT_COST_CMCC, upCmccInt);
        	}
        	else
        	{
        		context.put(INT_UPAGENT_COST_CTCC,  Integer.parseInt(upAgentCost.getValue()));
        		context.put(INT_UPAGENT_COST_CUCC,  Integer.parseInt(upAgentCost.getValue()));
        		context.put(INT_UPAGENT_COST_CMCC,  Integer.parseInt(upAgentCost.getValue()));
        	}
            /**
             * @end
             */
      	}
        
        return false;
    }
}
