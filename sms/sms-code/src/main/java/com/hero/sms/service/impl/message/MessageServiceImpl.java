package com.hero.sms.service.impl.message;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.commands.utils.ChainUtil;
import com.hero.sms.entity.message.SimpleNote;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.message.IMessageService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.service.organization.IOrganizationService;

@Service
public class MessageServiceImpl implements IMessageService {

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private IAgentService agentService;

    @Autowired
    private ISendRecordService sendRecordService;

    @Autowired
    private ISmsChannelService smsChannelService;

    @SuppressWarnings("unchecked")
	@Override
    @Transactional
    public void send(SimpleNote simpleNote) {

        //加载chain
        Command cmd = null;
        try {
            cmd = ChainUtil.getChain("sendMessageFromMQ");
            Context context = new ContextBase();
            context.put(BaseCommand.OBJ_REQ_SIMPLENOTE,simpleNote);
            context.put(BaseCommand.OBJ_ORG_SERVICE,this.organizationService);
            context.put(BaseCommand.OBJ_AGENT_SERVICE,this.agentService);
            context.put(BaseCommand.OBJ_SENDRECORD_SERVICE,sendRecordService);
            context.put(BaseCommand.OBJ_CHANNEL_SERVICE,smsChannelService);
            boolean result = cmd.execute(context);
            if (result){//流程没有全部走完
                String errorStr = context.get(BaseCommand.STR_ERROR_INFO).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
