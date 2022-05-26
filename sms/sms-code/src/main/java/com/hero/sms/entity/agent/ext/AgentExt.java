package com.hero.sms.entity.agent.ext;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.utils.StringUtil;

import lombok.Data;

@Data
public class AgentExt extends Agent {

    private List<AgentCost> costs;

    public AgentCost getAgentCost(Integer smsType,String costName){
        if (costs == null || costs.size() == 0){
            return null;
        }
        List<AgentCost> tmp = costs.stream()
                .filter((agentCost) -> agentCost.getSmsType().intValue() == smsType.intValue() && agentCost.getName().equals(costName) && StringUtil.isBlank(agentCost.getOperator()))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(tmp))  return tmp.get(0);
        return null;
    }
    
    /**
     * @begin 2021-11-09
     * @param smsType
     * @param costName
     * @param operator 电信 CTCC、联通 CUCC、移动 CMCC
     * @return
     */
    public AgentCost getAgentCost(Integer smsType,String costName,String operator){
        if (costs == null || costs.size() == 0){
            return null;
        }
        List<AgentCost> tmp = costs.stream()
                .filter((agentCost) -> agentCost.getSmsType().intValue() == smsType.intValue() && agentCost.getName().equals(costName)&& operator.equals(agentCost.getOperator()))
                .collect(Collectors.toList());
        //若针对运营商的配置未获得，则取通用配置
        if(CollectionUtils.isEmpty(tmp)) 
        {
        	return getAgentCost(smsType, costName);
        }
        if(CollectionUtils.isNotEmpty(tmp))  return tmp.get(0);
        return null;
    }
    
    /**
     * @begin 2021-11-13
     * @param smsType
     * @param costName
     * @return
     */
    public List<AgentCost> getAgentCostList(Integer smsType,String costName){
        if (costs == null || costs.size() == 0){
            return null;
        }
        List<AgentCost> tmp = costs.stream()
                .filter((agentCost) -> agentCost.getSmsType().intValue() == smsType.intValue() && agentCost.getName().equals(costName) && StringUtil.isNotBlank(agentCost.getOperator()))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(tmp))  return tmp;
        return null;
    }
}
