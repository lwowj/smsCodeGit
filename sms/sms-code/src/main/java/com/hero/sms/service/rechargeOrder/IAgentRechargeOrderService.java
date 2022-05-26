package com.hero.sms.service.rechargeOrder;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.entity.rechargeOrder.StatisticBean;

import java.util.Date;
import java.util.List;

/**
 * 代理充值订单 Service接口
 *
 * @author Administrator
 * @date 2020-03-12 18:00:59
 */
public interface IAgentRechargeOrderService extends IService<AgentRechargeOrder> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentRechargeOrder agentRechargeOrder
     * @return IPage<AgentRechargeOrder>
     */
    IPage<AgentRechargeOrder> findAgentRechargeOrders(QueryRequest request, AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime);

    /**
     * 根据代理查询代理充值订单
     * @param request
     * @param agentRechargeOrder
     * @param agentReqStartTime
     * @param agentReqEndTime
     * @param upAgent
     * @return
     */
    IPage<AgentRechargeOrder> findAgentRechargeOrders(QueryRequest request, AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime, Agent upAgent);

    /**
     * 查询（所有）
     *
     * @param agentRechargeOrder agentRechargeOrder
     * @return List<AgentRechargeOrder>
     */
    List<AgentRechargeOrder> findAgentRechargeOrders(AgentRechargeOrder agentRechargeOrder);

    /**
     * 代理端充值
     * @param agentRechargeOrder
     * @param upAgent
     * @param payPassword
     */
    void createAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder, Agent upAgent, String payPassword);

    /**
     * 新增
     *
     * @param agentRechargeOrder agentRechargeOrder
     */
    void createAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder);

    /**
     * 扣除额度
     * @param agentRechargeOrder
     */
    void deducting(AgentRechargeOrder agentRechargeOrder);

    /**
     * 修改
     *
     * @param agentRechargeOrder agentRechargeOrder
     */
    void updateAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder);

    /**
     * 删除
     *
     * @param agentRechargeOrder agentRechargeOrder
     */
    void deleteAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder);

    /**
    * 删除
    *
    * @param agentRechargeOrderIds agentRechargeOrderIds
    */
    void deleteAgentRechargeOrders(String[] agentRechargeOrderIds);

    /**
     * 代理充值订单统计
     * @param agentRechargeOrder
     * @param agentReqStartTime
     * @param agentReqEndTime
     * @return
     */
    List<StatisticBean> statisticBeanList(AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime);
}
