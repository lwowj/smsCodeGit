package com.hero.sms.commands.reSend;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.service.message.ISendBoxService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Context;

import java.util.Date;
import java.util.List;

@Slf4j
public class SaveSendBox extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        ISendBoxService sendBoxService = (ISendBoxService)context.get(OBJ_SENDBOX_SERVICE);
        SendBox saveSendBox = (SendBox) context.get(OBJ_SAVE_SENDBOX_ENTITY);
        try {
            sendBoxService.createSendBox(saveSendBox);
        } catch (ServiceException e) {
            context.put(STR_ERROR_INFO,e.getMessage());
            return true;
        }catch (Exception e1) {
            log.error("保存发件箱失败",e1);
            context.put(STR_ERROR_INFO,"提交失败");
            return true;
        }
        if(saveSendBox.getAuditState().intValue() == AuditStateEnums.Pass.getCode().intValue()) {
            if(saveSendBox.getTimingTime() == null) {
                if (saveSendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()){
                    List<Object> excelModels = (List<Object>) context.get(LIST_SAVE_MODEL);
                    sendBoxService.splitRecordForExcel(saveSendBox, excelModels);
                }else if (saveSendBox.getType().intValue() == SendBoxTypeEnums.formSubmit.getCode()){
                    List<String> smsNumberList = (List<String>) context.get(LIST_SAVE_MODEL);
                    sendBoxService.splitRecordForTxt(saveSendBox, smsNumberList);
                }

            }
        }
        return false;
    }
}
