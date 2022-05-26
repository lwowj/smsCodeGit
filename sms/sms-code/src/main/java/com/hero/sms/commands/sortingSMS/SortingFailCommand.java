package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.NotifyMsgStateModel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.organization.IOrganizationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SortingFailCommand extends BaseCommand {

	@Override
	public boolean execute(Context context) throws Exception {
		try {
			IOrganizationService organizationService = (IOrganizationService)context.get(OBJ_ORG_SERVICE);
			OrganizationExt orgExt = (OrganizationExt)context.get(OBJ_QUERY_ORG);
			SendRecord sendRecord = (SendRecord)context.get(OBJ_SAVE_SENDRECORD_ENTITY);
			sendRecord.setState(SendRecordStateEnums.SortingFail.getCode());
			sendRecord.setStateDesc(context.get(STR_ERROR_INFO).toString());
			
			String notifyMsg = (String)context.get(STR_NOTIFY_MSG);
			if(StringUtils.isBlank(notifyMsg)) {
				notifyMsg = "服务异常";
			}

			NotifyMsgStateModel model = new NotifyMsgStateModel();
			model.setMobileArea(sendRecord.getSmsNumberArea());
			model.setMobile(sendRecord.getSmsNumber());
			model.setMsg(notifyMsg);
			model.setState(String.valueOf(SendRecordStateEnums.SortingFail.getCode()));
			model.setSubType(sendRecord.getSubType());
			model.setSendCode(sendRecord.getSendCode());
			model.setOrgCode(orgExt.getOrganizationCode());
			if(sendRecord.getReceiptTime() != null) {
				model.setReceiptTime(sendRecord.getReceiptTime());
			}else {
				model.setReceiptTime(sendRecord.getCreateTime());
			}
			organizationService.notifyMsgState(orgExt, model);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

}
