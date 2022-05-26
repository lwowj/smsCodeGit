package com.hero.sms.service.agent;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenuLimit;

/**
 * 代理菜单关联表 Service接口
 *
 * @author Administrator
 * @date 2020-03-06 10:05:54
 */
public interface IAgentMenuLimitService extends IService<AgentMenuLimit> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentMenuLimit agentMenuLimit
     * @return IPage<AgentMenuLimit>
     */
    IPage<AgentMenuLimit> findAgentMenuLimits(QueryRequest request, AgentMenuLimit agentMenuLimit);

    /**
     * 查询（所有）
     *
     * @param agentMenuLimit agentMenuLimit
     * @return List<AgentMenuLimit>
     */
    List<AgentMenuLimit> findAgentMenuLimits(AgentMenuLimit agentMenuLimit);

    /**
     * 根据代理ID查询
     * @param agentId
     * @return
     */
    List<AgentMenuLimit> findAgentMenuLimitsByAgentId(int agentId);

    /**
     * 新增
     *
     * @param agentMenuLimit agentMenuLimit
     */
    void createAgentMenuLimit(AgentMenuLimit agentMenuLimit);

    /**
     * 修改
     *
     * @param agentMenuLimit agentMenuLimit
     */
    void updateAgentMenuLimit(AgentMenuLimit agentMenuLimit);

    /**
     * 代理端修改权限
     * @param menuIds
     * @param agentId
     * @param upAgent
     */
    void updateAgentMenuLimits(String menuIds, Long agentId, Agent upAgent);

    /**
     * 批量修改
     * @param menuIds
     * @param agentId
     */
    @Transactional
    void updateAgentMenuLimits(String menuIds,Long agentId);

    /**
     * 删除
     *
     * @param agentMenuLimit agentMenuLimit
     */
    void deleteAgentMenuLimit(AgentMenuLimit agentMenuLimit);
}
