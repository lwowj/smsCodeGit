package com.hero.sms.commands.sortingSMS;

import java.util.List;
import java.util.Optional;

import org.apache.commons.chain.Context;
import org.apache.commons.collections4.CollectionUtils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelCost;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.channel.ISmsChannelCostService;
import com.hero.sms.utils.StringUtil;

/**
 * 获取通道资费
 * @author Lenovo
 *
 */
public class QueryChannelCostCommand extends BaseCommand {

    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
    	SmsChannel smsChannel = (SmsChannel) context.get(OBJ_SMS_CHANNEL);
    	SendRecord sendRecord = (SendRecord) context.get(OBJ_SAVE_SENDRECORD_ENTITY);
    	ISmsChannelCostService smsChannelCastService = (ISmsChannelCostService) context.get(OBJ_CHANNEL_COST_SERVICE);
    	SmsChannelCost smsChannelCost = null;
		SmsChannelCost queryChannelCost = new SmsChannelCost();
		queryChannelCost.setSmsChannelId(smsChannel.getId());
		List<SmsChannelCost> costs = smsChannelCastService.findSmsChannelCosts(queryChannelCost);
		if(CollectionUtils.isNotEmpty(costs)) 
		{
			Optional<SmsChannelCost> findAny =  null;
			 /**
	         * @begin 2021-11-09
	         * 根据运营商获取资费
	         */
			String smsNumberArea = sendRecord.getSmsNumberArea();
			if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()))
			{
				//通道费率是否要配置运营商
		        String operator = sendRecord.getSmsNumberOperator();
		        if(StringUtil.isNotBlank(operator))
		        {
		        	findAny = costs.stream().filter(cost -> cost.getSmsType().intValue() == sendRecord.getSmsType().intValue() 
							&& cost.getName().equals(sendRecord.getSmsNumberArea())&& operator.equals(cost.getOperator())).findAny();
		        }
				//如果根据运营商编码未获取到通道资费，则取通用资费
				if(findAny == null ||!findAny.isPresent())
				{
					findAny = costs.stream().filter(cost -> cost.getSmsType().intValue() == sendRecord.getSmsType().intValue() 
							&& cost.getName().equals(sendRecord.getSmsNumberArea()) && StringUtil.isBlank(cost.getOperator())).findAny();
				}
			}
			else
			{
				findAny = costs.stream().filter(cost -> cost.getSmsType().intValue() == sendRecord.getSmsType().intValue() 
						&& cost.getName().equals(sendRecord.getSmsNumberArea())&& StringUtil.isBlank(cost.getOperator())).findAny();
			}
			/**
	         * @end
	         */
			if(findAny != null && findAny.isPresent()) {
				smsChannelCost = findAny.get();
//				smsChannelCostMap.put(smsChannel.getId(), smsChannelCost);
			}
		}
	
		
    	if(smsChannelCost == null) {
    		context.put(STR_ERROR_INFO, String.format("通道【%s】未配置【%s】【%s】费率",smsChannel.getName(),SmsTypeEnums.getNameByCode(sendRecord.getSmsType()),SmsNumberAreaCodeEnums.getNameByCode(sendRecord.getSmsNumberArea())));
    		return true;
    	}
    	
    	String cost = smsChannelCost.getValue();
    	if(StringUtils.isEmpty(cost) || Integer.parseInt(cost) <= 0) {
    		context.put(STR_ERROR_INFO, String.format("通道【%s】【%s】【%s】费率配置错误",smsChannel.getName(),SmsTypeEnums.getNameByCode(sendRecord.getSmsType()),SmsNumberAreaCodeEnums.getNameByCode(sendRecord.getSmsNumberArea())));
    		return true;
    	}
        context.put(INT_SMS_CHANNEL_COST, Integer.parseInt(cost));
//        context.put(INT_SMS_CHANNEL_COST_ID, smsChannelCost.getId());
        return false;
    }
}
