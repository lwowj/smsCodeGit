package com.hero.sms.mapper.agent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.ext.AgentExt;

/**
 * 商户代理 Mapper
 *
 * @author Administrator
 * @date 2020-03-06 10:05:11
 */
public interface AgentMapper extends BaseMapper<Agent> {

    /**
     * 根据ID 级联查询代理及费率信息
     * @param id
     * @return
     */
    public AgentExt queryAgentExtById(Integer id);

}
