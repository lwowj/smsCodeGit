package com.hero.sms.service.impl.channel.payTransfer;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ContextBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.commands.pay.PayBaseCommand;
import com.hero.sms.commands.utils.ChainUtil;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.service.channel.IPayChannelService;
import com.hero.sms.service.channel.payTransfer.IPayService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;

/**
 * 交易
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class PayServiceImpl implements IPayService {

    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IPayChannelService payChannelService;
    @Autowired
    private IOrganizationRechargeOrderService organizationRechargeOrderService;

    /**
     * 商户后台充值交易
     * @return
     */
    @Override
    @Transactional
    public FebsResponse pay(String data){
        FebsResponse result = new FebsResponse();
        result.success();
        try {
            ContextBase context = new ContextBase();
            context.put(PayBaseCommand.PAY_DATA,JSONObject.parseObject(data));
            context.put(PayBaseCommand.ORG_SERVICE,organizationService);
            context.put(PayBaseCommand.PAY_CHANNEL_SERVICE,payChannelService);
            context.put(PayBaseCommand.RECHARGE_ORDER_SERVICE,organizationRechargeOrderService);
            Command cmd = ChainUtil.getChain(PayBaseCommand.PAY_COMMAND_NAME);
            if(cmd.execute(context)){
                result.message((String) context.get(PayBaseCommand.STR_ERROR_INFO));
            }
            result.put(PayBaseCommand.PAY_URL_TYPE,context.get(PayBaseCommand.PAY_URL_TYPE));
            result.put(PayBaseCommand.PAY_URL,context.get(PayBaseCommand.PAY_URL));
            return result;
        }catch (Exception e){
            result.message("提交失败，请稍后重新尝试！");
        }
        return result;
    }

    /**
     * 商户后台充值交易回调
     * @return
     */
    @Override
    @Transactional
    public FebsResponse payResult(String data) throws Exception{
        FebsResponse result = new FebsResponse();
        ContextBase context = new ContextBase();
        context.put(PayBaseCommand.PAY_DATA,JSONObject.parseObject(data));
        context.put(PayBaseCommand.ORG_SERVICE,organizationService);
        context.put(PayBaseCommand.PAY_CHANNEL_SERVICE,payChannelService);
        context.put(PayBaseCommand.RECHARGE_ORDER_SERVICE,organizationRechargeOrderService);
        Command cmd = ChainUtil.getChain(PayBaseCommand.PAY_RESULT_COMMAND_NAME);
        if(cmd.execute(context)){
            result.message((String) context.get(PayBaseCommand.STR_ERROR_INFO));
        }
        return result;
    }

}
