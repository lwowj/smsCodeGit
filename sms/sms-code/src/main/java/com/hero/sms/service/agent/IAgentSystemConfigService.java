package com.hero.sms.service.agent;


import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentSystemConfig;

/**
 * 代理系统配置 Service接口
 *
 * @author Administrator
 * @date 2020-03-18 12:10:37
 */
public interface IAgentSystemConfigService extends IService<AgentSystemConfig> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentSystemConfig agentSystemConfig
     * @return IPage<AgentSystemConfig>
     */
    IPage<AgentSystemConfig> findAgentSystemConfigs(QueryRequest request, AgentSystemConfig agentSystemConfig);

    /**
     * 查询（所有）
     *
     * @param agentSystemConfig agentSystemConfig
     * @return List<AgentSystemConfig>
     */
    List<AgentSystemConfig> findAgentSystemConfigs(AgentSystemConfig agentSystemConfig);

    /**
     * 根据代理ID获取
     * @param agentSystemConfig
     * @param agentId
     * @return
     */
    AgentSystemConfig findAgentSystemConfig(AgentSystemConfig agentSystemConfig, Integer agentId);

    /**
     * 新增
     *
     * @param agentSystemConfig agentSystemConfig
     */
    void createAgentSystemConfig(AgentSystemConfig agentSystemConfig);

    /**
     * 代理端新增
     * @param agentSystemConfig
     * @param agent
     */
    @Transactional
    void createAgentSystemConfig(AgentSystemConfig agentSystemConfig, Agent agent);

    /**
     * 修改
     *
     * @param agentSystemConfig agentSystemConfig
     */
    void updateAgentSystemConfig(AgentSystemConfig agentSystemConfig);

    /**
     * 删除
     *
     * @param agentSystemConfig agentSystemConfig
     */
    void deleteAgentSystemConfig(AgentSystemConfig agentSystemConfig);

    /**
    * 删除
    *
    * @param agentSystemConfigIds agentSystemConfigIds
    */
    void deleteAgentSystemConfigs(String[] agentSystemConfigIds);

    /**
     * 批量认证代理配置
     * @param configIds
     * @param approveState
     */
    void approveAgentConfigs(String[] configIds, String approveState);
}
