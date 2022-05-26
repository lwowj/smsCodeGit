package com.hero.sms.commands.pay;

import org.apache.commons.chain.Context;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.common.DatabaseCache;

/**
 * 判断充值网关
 */
public class CheckNetwayCodeCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        JSONObject data = (JSONObject) context.get(PAY_DATA);
        String netwayCode = data.getString(NETWAY_CODE);
        if(DatabaseCache.getCodeBySortCodeAndCode("NetwayCode",netwayCode) !=null){
            context.put(NETWAY_CODE,netwayCode);
            return false;
        }
        context.put(STR_ERROR_INFO,"充值方式错误！");
        return true;
    }
}
