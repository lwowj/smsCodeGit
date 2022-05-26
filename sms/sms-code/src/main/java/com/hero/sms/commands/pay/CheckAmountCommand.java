package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.alibaba.fastjson.JSONObject;

/**
 * 校验充值金额
 */
public class CheckAmountCommand extends PayBaseCommand{
    @Override
    public boolean execute(Context context) throws Exception {
        JSONObject data = (JSONObject) context.get(PAY_DATA);
        Integer amount = Integer.parseInt(data.getString("rechargeAmount"));
        if(amount < 100){
            context.put(STR_ERROR_INFO,"充值金额不能低于一元");
            return true;
        }
        context.put(AMOUNT,amount);
        return false;
    }
}
