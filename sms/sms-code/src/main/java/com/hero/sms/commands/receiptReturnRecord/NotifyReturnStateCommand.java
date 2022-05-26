package com.hero.sms.commands.receiptReturnRecord;

import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Maps;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.NotifyMsgStateModel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.organization.IOrganizationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotifyReturnStateCommand extends BaseCommand {

	@Override
	public boolean execute(Context context) throws Exception {
		IOrganizationService organizationService = (IOrganizationService)context.get(OBJ_ORG_SERVICE);
		List<SendRecord> sendRecords = (List<SendRecord>)context.get(LIST_SEND_RECORD);
		if(CollectionUtils.isNotEmpty(sendRecords)) {
			for (SendRecord sendRecord : sendRecords) {
				if(sendRecord==null)
				{
					continue;
				}
				Integer subType = 1;
				try {
					subType = sendRecord.getSubType();
				} catch (Exception e) {
					// TODO: handle exception
					log.error(String.format("【NotifyReturnStateCommand】回执处理失败，异常数据：【%s】", JSON.toJSONString(sendRecord)), e);
					subType = 1;
					throw e;
				} 
				if(SendBoxSubTypeEnums.OrgWebSub.getCode().intValue() == subType.intValue()) {
					continue;
				}
				String orgCode = sendRecord.getOrgCode();
				Map<String, Organization> orgMap = Maps.newHashMap();
				if(StringUtils.isBlank(orgCode)) {
					continue;
				}
				Organization org = orgMap.get(orgCode);
				if(org == null) {
					org = organizationService.getOrganizationByCode(orgCode);
				}
				if(org == null) {
					continue;
				}
				orgMap.put(orgCode, org);
				NotifyMsgStateModel model = new NotifyMsgStateModel();
				model.setSourceNumber(sendRecord.getSourceNumber());
				model.setMobile(sendRecord.getSmsNumber());
				model.setMobileArea(sendRecord.getSmsNumberArea());
				model.setOrgCode(orgCode);
				model.setSendCode(sendRecord.getSendCode());
				model.setSubType(subType);
				 if(sendRecord.getReceiptTime() != null) {
					 model.setReceiptTime(sendRecord.getReceiptTime());
	                }else {
	                	model.setReceiptTime(sendRecord.getCreateTime());
	                }
				
				Integer state = sendRecord.getState();
				if(state.intValue() == SendRecordStateEnums.ReceiptSuccess.getCode()) {
					model.setState(String.valueOf(state));
					model.setMsg(SendRecordStateEnums.ReceiptSuccess.getMsg());
				}else if(state.intValue() == SendRecordStateEnums.ReceiptFail.getCode()) {
					model.setState(String.valueOf(state));
					model.setMsg(SendRecordStateEnums.SortingFail.getMsg());
				}else if(state.intValue() == SendRecordStateEnums.ReceiptFail.getCode()) {
					model.setState(String.valueOf(state));
					model.setMsg(sendRecord.getStateDesc());
				}else{
					continue;
				}
				//4-1-1-1-1 将结果按我方协议进行封装，并推送到MQ
				organizationService.notifyMsgState(org, model);
			}
		}
		return false;
	}

}
