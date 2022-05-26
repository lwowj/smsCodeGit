package com.hero.sms.commands.sortingSMS;

import java.util.Date;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.agent.ext.AgentExt;

/**
 * 判断代理是否达到日限量，超量则分拣失败
 */
public class AgentDayLimitCommand extends BaseCommand {

    private String AGENT_DAY_INC_KEY_FORMAT = "AGNET%s_%s";

    @Override
    public boolean execute(Context context) throws Exception {
        AgentExt agent = (AgentExt) context.get(OBJ_QUERY_AGENT);
        AgentExt upAgent = (AgentExt) context.get(OBJ_QUERY_UPAGENT);

        AgentExt topAgent = (upAgent!=null?upAgent:agent);
        //redis 顶级代理当日发送量记录的KEY初始化
        String key = String.format(AGENT_DAY_INC_KEY_FORMAT
                ,topAgent.getId()
                ,DateUtil.getString(new Date(), "yyyyMMdd"));
        RedisHelper redisHelper = SpringContextUtil.getBean(RedisHelper.class);
        if (topAgent.getDayLimit() != null && topAgent.getDayLimit() > 0L){
            //代理设置了日限量
            Integer result = (Integer) redisHelper.get(key);
            if (result != null && result >= topAgent.getDayLimit()){
                context.put(STR_ERROR_INFO, "短信日余量不足");
                return true;
            }
        }
        context.put(TOPAGENT_DAY_INC_KEY,key);
        return false;
    }

}
