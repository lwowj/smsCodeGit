package com.hero.sms.commands.reSend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Context;

import java.util.Date;

@Slf4j
public class QueryOldSendBox extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        ISendBoxService sendBoxService = (ISendBoxService)context.get(OBJ_SENDBOX_SERVICE);
        String sendCode = (String) context.get(STR_SENDCODE);
        //查询发件箱信息
        LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseSend::getSendCode,sendCode);
        SendBox oldSendBox = sendBoxService.getOne(queryWrapper);
        if (oldSendBox == null){
            context.put(STR_ERROR_INFO,String.format("[发件箱重发]未找到批次号为(%s)的发件箱记录",sendCode));
            return true;
        }

        context.put(OBJ_SENDBOX_ENTITY,oldSendBox);

        //复制属性
        SendBox saveSendBox = (SendBox) context.get(OBJ_SAVE_SENDBOX_ENTITY);
        saveSendBox.setSubType(oldSendBox.getSubType());
        saveSendBox.setOrgCode(oldSendBox.getOrgCode());
        saveSendBox.setAgentId(oldSendBox.getAgentId());
        saveSendBox.setSmsType(oldSendBox.getSmsType());
        saveSendBox.setType(oldSendBox.getType());
        saveSendBox.setCreateTime(new Date());
        saveSendBox.setSmsContent(oldSendBox.getSmsContent());

        return false;
    }
}
