package com.hero.sms.commands.reSend;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.organization.IOrganizationService;

public class CheckOrgAmout extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        SendBox saveSendBox = (SendBox) context.get(OBJ_SAVE_SENDBOX_ENTITY);
        IOrganizationService organizationService = (IOrganizationService) context.get(OBJ_ORG_SERVICE);
        //商户资费及余额预检查
        boolean result = false;
        try {
//            result = organizationService.checkOrgAmountPrePay(saveSendBox.getOrgCode()
//                    , SmsTypeEnums.TextMsg.getCode(), SmsNumberAreaCodeEnums.China.getInArea(), saveSendBox.getNumberCount());
        	if(saveSendBox.getSmsNumberArea()==null||"".equals(saveSendBox.getSmsNumberArea()))
        	{
        		saveSendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
        	}
          result = organizationService.checkOrgAmountPrePay(saveSendBox.getOrgCode()
          , SmsTypeEnums.TextMsg.getCode(), saveSendBox.getSmsNumberArea(), saveSendBox.getNumberCount());
            if (!result){
               context.put(STR_ERROR_INFO,"商户余额不足");
               return true;
            }
        } catch (ServiceException e) {
            context.put(STR_ERROR_INFO,e.getMessage());
            return true;
        }
        return false;
    }
}
