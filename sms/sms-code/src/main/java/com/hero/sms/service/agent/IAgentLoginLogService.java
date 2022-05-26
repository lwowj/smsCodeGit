package com.hero.sms.service.agent;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.AgentLoginLog;
import com.hero.sms.entity.agent.AgentLoginLogQuery;

/**
 * 代理登录日志表 Service接口
 *
 * @author Administrator
 * @date 2020-12-25 17:32:55
 */
public interface IAgentLoginLogService extends IService<AgentLoginLog> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentLoginLog agentLoginLog
     * @return IPage<AgentLoginLog>
     */
    IPage<AgentLoginLog> findAgentLoginLogs(QueryRequest request, AgentLoginLog agentLoginLog);

    /**
     * 查询（所有）
     *
     * @param agentLoginLog agentLoginLog
     * @return List<AgentLoginLog>
     */
    List<AgentLoginLog> findAgentLoginLogs(AgentLoginLog agentLoginLog);
    
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentLoginLog agentLoginLog
     * @return IPage<AgentLoginLog>
     */
    IPage<AgentLoginLog> findAgentLoginLogs(QueryRequest request, AgentLoginLogQuery agentLoginLog);

    /**
     * 查询（所有）
     *
     * @param agentLoginLog agentLoginLog
     * @return List<AgentLoginLog>
     */
    List<AgentLoginLog> findAgentLoginLogs(AgentLoginLogQuery agentLoginLog);

    /**
     * 新增
     *
     * @param agentLoginLog agentLoginLog
     */
    void createAgentLoginLog(AgentLoginLog agentLoginLog);

    /**
     * 修改
     *
     * @param agentLoginLog agentLoginLog
     */
    void updateAgentLoginLog(AgentLoginLog agentLoginLog);

    /**
     * 删除
     *
     * @param agentLoginLog agentLoginLog
     */
    void deleteAgentLoginLog(AgentLoginLog agentLoginLog);

    /**
    * 删除
    *
    * @param agentLoginLogIds agentLoginLogIds
    */
    void deleteAgentLoginLogs(String[] agentLoginLogIds);
    
    /**
     * 保存代理登录日志
     * @param start
     * @param agentLoginLog
     */
    void saveLog(long start, AgentLoginLog agentLoginLog);
}
