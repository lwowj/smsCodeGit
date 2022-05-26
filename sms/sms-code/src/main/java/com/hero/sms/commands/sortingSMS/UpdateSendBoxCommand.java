package com.hero.sms.commands.sortingSMS;

import java.util.Date;

import org.apache.commons.chain.Context;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.service.message.ISendBoxService;

import lombok.extern.slf4j.Slf4j;

/**
 * 更新发件箱数据
 * @author Lenovo
 *
 */
@Slf4j
public class UpdateSendBoxCommand  extends BaseCommand {
	
	@Override
	public boolean execute(Context context) throws Exception {
		ISendBoxService sendBoxService = (ISendBoxService) context.get(OBJ_SENDBOX_SERVICE);
		SendBox sendBox = (SendBox) context.get(OBJ_SENDBOX_ENTITY);
		SendBox updateSendBox = new SendBox();
		updateSendBox.setId(sendBox.getId());
		LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();
		/*updateWrapper.set(SendBox::getChannelCostAmount, sendBox.getChannelCostAmount());
		updateWrapper.set(SendBox::getAgentIncomeAmount, sendBox.getAgentIncomeAmount());
		updateWrapper.set(SendBox::getIncomeAmount, sendBox.getIncomeAmount());*/
		updateWrapper.set(SendBox::getSortingTime,new Date());
		updateWrapper.set(SendBox::getTimingTime,null);
		
		updateWrapper.setSql("sms_count=sms_count+"+sendBox.getSmsCount());
		updateWrapper.setSql("consume_amount=consume_amount+"+sendBox.getConsumeAmount());
		updateWrapper.setSql("channel_cost_amount=channel_cost_amount+"+sendBox.getChannelCostAmount());
		updateWrapper.setSql("agent_income_amount=agent_income_amount+"+sendBox.getAgentIncomeAmount());
		updateWrapper.setSql("up_agent_income_amount=up_agent_income_amount+"+sendBox.getUpAgentIncomeAmount());
		updateWrapper.setSql("income_amount=income_amount+"+sendBox.getIncomeAmount());
		updateWrapper.eq(SendBox::getId, sendBox.getId());
		sendBoxService.update(updateWrapper);
		return false;
	}

}
