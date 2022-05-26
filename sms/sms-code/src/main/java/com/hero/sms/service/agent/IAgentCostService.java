package com.hero.sms.service.agent;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.entity.channel.AreaCode;

/**
 * 商户代理资费 Service接口
 *
 * @author Administrator
 * @date 2020-03-06 10:05:33
 */
public interface IAgentCostService extends IService<AgentCost> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentCost agentCost
     * @return IPage<AgentCost>
     */
    IPage<AgentCost> findAgentCosts(QueryRequest request, AgentCost agentCost);

    /**
     * 查询（所有）
     *
     * @param agentCost agentCost
     * @return List<AgentCost>
     */
    List<AgentCost> findAgentCosts(AgentCost agentCost);

    /**
     * 新增
     *
     * @param agentCost agentCost
     */
    void createAgentCost(AgentCost agentCost);

    /**
     * 修改
     *
     * @param agentCost agentCost
     */
    void updateAgentCost(AgentCost agentCost);

    /**
     * 代理端添加资费
     * @param agentCosts
     * @param upAgent
     */
    void updateAgentCosts(List<AgentCost> agentCosts, Agent upAgent);

    /**
     * 批量修改
     *
     * @param agentCost agentCosts
     */
    void updateAgentCosts(List<AgentCost> agentCost,String userName);

    /**
     * 删除
     *
     * @param agentCost agentCost
     */
    void deleteAgentCost(AgentCost agentCost);
    
    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     */
    void updateCosts(String agentIds, String costName, String smsType, String costValue);
    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     */
    void updateCosts(String agentIds, String costName, String smsType, String costValue,String operator,String userName);
    /**
     * 批量新增资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     */
    void addAgentCosts(String agentIds, String costName,String smsType, String costValue,String operator,String userName);
    /**
     * 批量新增资费
     * @param costs
     */
    void addAgentCosts(List<AgentCost> agentCosts);
    /**
     * 批量浮动更新资费
     * @param agentIds
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     * @param updateType
     */
    void updateFloatCosts(String agentIds, String costName,String smsType, String costValue,String operator,String userName,String updateType);

    /**
     * 筛选已配置的地区列表
     * @param agentId
     * @return
     */
    List<AreaCode> getAgentAreaCodeList(int agentId);
}
