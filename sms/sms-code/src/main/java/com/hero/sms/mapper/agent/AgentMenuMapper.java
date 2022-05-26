package com.hero.sms.mapper.agent;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.sms.entity.agent.AgentMenu;

/**
 * 代理菜单表 Mapper
 *
 * @author Administrator
 * @date 2020-03-06 10:05:44
 */
public interface AgentMenuMapper extends BaseMapper<AgentMenu> {

    /**
     * 根据登录名查询权限集
     * @param agentAccount
     * @return
     */
    List<AgentMenu> findAgentPermissions(String agentAccount);

    /**
     * 根据登录查询菜单集
     * @param agentAccount
     * @return
     */
    List<AgentMenu> findAgentMenus(String agentAccount);

}
