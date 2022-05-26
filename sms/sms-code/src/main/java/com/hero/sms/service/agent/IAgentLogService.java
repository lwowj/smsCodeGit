package com.hero.sms.service.agent;


import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentLog;

/**
 * 代理操作日志表 Service接口
 *
 * @author Administrator
 * @date 2020-04-02 11:01:33
 */
public interface IAgentLogService extends IService<AgentLog> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentLog agentLog
     * @return IPage<AgentLog>
     */
    IPage<AgentLog> findAgentLogs(QueryRequest request, AgentLog agentLog, Date agentLogStartTime, Date agentLogEndTime);

    /**
     * 查询（所有）
     *
     * @param agentLog agentLog
     * @return List<AgentLog>
     */
    List<AgentLog> findAgentLogs(AgentLog agentLog);

    /**
     * 新增
     *
     * @param agentLog agentLog
     */
    void createAgentLog(AgentLog agentLog);

    /**
     * 修改
     *
     * @param agentLog agentLog
     */
    void updateAgentLog(AgentLog agentLog);

    /**
     * 删除
     *
     * @param agentLog agentLog
     */
    void deleteAgentLog(AgentLog agentLog);

    /**
    * 删除
    *
    * @param agentLogIds agentLogIds
    */
    void deleteAgentLogs(String[] agentLogIds);

    /**
     * 保存代理操作日志
     * @param point
     * @param method
     * @param ip
     * @param operation
     * @param start
     */
    void saveLog(ProceedingJoinPoint point, Method method, String ip, String operation, long start, Agent user);
}
