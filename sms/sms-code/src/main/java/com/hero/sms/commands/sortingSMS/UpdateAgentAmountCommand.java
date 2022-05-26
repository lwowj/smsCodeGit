package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.impl.agent.AgentServiceImpl;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class UpdateAgentAmountCommand extends BaseCommand {
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
        SendBox sendBox = (SendBox) context.get(OBJ_SENDBOX_ENTITY);
        IAgentService agentService = (IAgentService) context.get(OBJ_AGENT_SERVICE);
        Integer agentIncomeAmount = sendBox.getAgentIncomeAmount();
        if(agentIncomeAmount<0) {
        	String errMsg = String.format("代理【%d】利润金额【%f】异常",sendBox.getAgentId(),agentIncomeAmount/100.0);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
        }
        Integer agentId = sendBox.getAgentId();
        if(agentId == null) {
        	return false;
        }
        Agent agent = agentService.getById(agentId);
        if(agent == null) {
        	String errMsg = String.format("代理【%d】不存在",agentId);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
        }
        
        String agentMd5Data = agentService.getDataMd5(agent);
        if (!agent.getDataMd5().equals(agentMd5Data)){
        	String errMsg = String.format("代理【%d】信息安全校验失败",agentId);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
        }
        
        if(agentIncomeAmount > 0) {
        	LambdaUpdateWrapper<Agent> updateAgentWrapper = new LambdaUpdateWrapper<>();
        	updateAgentWrapper.setSql("available_amount=available_amount+"+agentIncomeAmount);
        	updateAgentWrapper.setSql("send_sms_total=send_sms_total+"+sendBox.getSmsCount());
        	updateAgentWrapper.setSql("data_md5=UPPER(MD5(CONCAT('"+AgentServiceImpl.Md5Key+"',agent_account,amount,Available_Amount,Cash_Amount,Send_Sms_Total,quota_amount)))");
        	updateAgentWrapper.eq(Agent::getId, agentId);
        	agentService.update(updateAgentWrapper);
        }
		
		
		Integer upAgentId = sendBox.getUpAgentId();
		if(upAgentId == null) {
			return false;
		}
		Integer upAgentIncomeAmount = sendBox.getUpAgentIncomeAmount();
		if(upAgentIncomeAmount < 0) {
			String errMsg = String.format("上级代理【%d】利润金额【%f】异常",sendBox.getAgentId(),upAgentIncomeAmount/100);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
		}
		
		Agent upAgent = agentService.getById(upAgentId);
		if(upAgent == null) {
			String errMsg = String.format("上级代理【%d】不存在",upAgentId);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
		}
		String upAgentMd5Data = agentService.getDataMd5(upAgent);
        if (!upAgent.getDataMd5().equals(upAgentMd5Data)){
        	String errMsg = String.format("代理【%d】信息安全校验失败",upAgentId);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
        }
        
        if(upAgentIncomeAmount > 0) {
        	LambdaUpdateWrapper<Agent> updateAgentWrapper = new LambdaUpdateWrapper<>();
        	updateAgentWrapper.setSql("available_amount=available_amount+"+upAgentIncomeAmount);
        	updateAgentWrapper.setSql("send_sms_total=send_sms_total+"+sendBox.getSmsCount());
        	updateAgentWrapper.setSql("data_md5=UPPER(MD5(CONCAT('"+AgentServiceImpl.Md5Key+"',agent_account,amount,Available_Amount,Cash_Amount,Send_Sms_Total,quota_amount)))");
        	updateAgentWrapper.eq(Agent::getId, upAgentId);
        	agentService.update(updateAgentWrapper);
        }
        return false;
    }
}
