package com.hero.sms.commands.apiSendBox;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISmsTemplateService;

public class CreateSendBoxCommand extends BaseCommand {

	@Override
	public boolean execute(Context context) throws Exception {
		ISmsTemplateService smsTemplateService = (ISmsTemplateService)context.get(OBJ_TEMPLATE_SERVICE);
		ISendBoxService sendBoxService = (ISendBoxService)context.get(OBJ_SENDBOX_SERVICE);
		SendBox sendBox = (SendBox)context.get(OBJ_BASESEND_ENTITY);
		OrganizationExt org = (OrganizationExt)context.get(OBJ_QUERY_ORG);
		sendBox.setAgentId(org.getAgentId());
		/*if(StringUtils.isBlank(sendBox.getSmsNumberArea())) sendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
		String content = sendBox.getSmsContent();
		SensitiveFilter sensitiveFilter = SensitiveFilter.DEFAULT;
		boolean isContainSens = sensitiveFilter.isContain(content);
		if(isContainSens) {
			context.put(STR_ERROR_INFO,String.format("短信内容包含敏感词==>【%s】",sensitiveFilter.filter(content, '*')));
			return true;
		}
		String orgCode = sendBox.getOrgCode();
		String smsApproveType = org.getSmsApproveType();
		boolean isNeedAudit = smsApproveType.equals(OrgSmsApproveTypeEnums.ManualAudit.getCode());
		if (smsApproveType.equals(OrgSmsApproveTypeEnums.TempNotAudit.getCode())) {
			isNeedAudit = true;
			SmsTemplate querySmsTemp = new SmsTemplate();
			querySmsTemp.setOrgCode(orgCode);
			querySmsTemp.setApproveStatus(AuditStateEnums.Pass.getCode());
			List<SmsTemplate> smsTemplates = smsTemplateService.findSmsTemplates(querySmsTemp);
			if (CollectionUtils.isNotEmpty(smsTemplates)) {
				String tmpContent = content;
				if(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
					tmpContent = content.replaceAll("##[B-I]{1}##", "#@#");
				}
				
				for (SmsTemplate smsTemplate : smsTemplates) {
					String tempContent = smsTemplate.getTemplateContent();
					tempContent = tempContent.replaceAll("\\{[0-9]+\\}", "#@#");
					if(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
						if(tmpContent.equals(tempContent)) {
							isNeedAudit = false;
							break;
						}
					}else {
						tempContent = AppUtil.escapeExprSpecialWord(tempContent);
						String regex = tempContent.replaceAll("#@#", ".*");
						boolean match = AppUtil.match(regex, content);
						if (match) {
							isNeedAudit = false;
							break;
						}
					}
				}
			}
		}
		if (isNeedAudit) {
			sendBox.setAuditState(AuditStateEnums.Wait.getCode());
		} else {
			sendBox.setAuditState(AuditStateEnums.Pass.getCode());
		}
		sendBox.setSmsCount(0);
		sendBox.setAgentId(org.getAgentId());
		sendBox.setCreateTime(new Date());
		sendBox.setSendCode(RandomUtil.randomStringWithDate(5));
		sendBoxService.save(sendBox);*/
		try {
			sendBoxService.createSendBox(sendBox);
		} catch (ServiceException se) {
			context.put(STR_ERROR_INFO, se.getMessage());
			return true;
		}
		return false;
	}
}
