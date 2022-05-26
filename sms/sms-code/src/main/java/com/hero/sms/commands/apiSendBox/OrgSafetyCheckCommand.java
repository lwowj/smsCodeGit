package com.hero.sms.commands.apiSendBox;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.message.ApiSendSmsModel;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.ext.OrganizationExt;

public class OrgSafetyCheckCommand extends BaseCommand {

	@Override
	public boolean execute(Context context) throws Exception {
		OrganizationExt org = (OrganizationExt) context.get(OBJ_QUERY_ORG);
		ApiSendSmsModel model = (ApiSendSmsModel) context.get(OBJ_API_SENDBOX);
		SendBox sendBox = (SendBox)context.get(OBJ_BASESEND_ENTITY);
		String bindIp = org.getBindIp();
		String clientIp = sendBox.getClientIp();
		if(StringUtils.isNotBlank(bindIp) && StringUtils.isNotBlank(clientIp)) {
			String[] bindIps = bindIp.split(",");
			boolean isHasIp = false;
			for (String ip : bindIps) {
				if(ip.equals(clientIp)) {
					isHasIp = true;
					break;
				}
			}
			if(!isHasIp) {
				context.put(STR_ERROR_INFO, "ip限制");
				return true;
			}
		}
		
		StringBuilder checkParam = new StringBuilder();
		checkParam.append(model.getOrgCode()).append("|").append(model.getPhones()).append("|").append(model.getMessage()).append("|").append(model.getRand()).append("|").append(org.getMd5Key());
		if(model.getSign().equals(MD5Util.MD5(checkParam.toString()))) {
			return false;
		}
		context.put(STR_ERROR_INFO, "签名失败");
		return true;
	}
}
