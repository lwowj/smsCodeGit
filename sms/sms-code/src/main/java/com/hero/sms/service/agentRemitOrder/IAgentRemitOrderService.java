package com.hero.sms.service.agentRemitOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agentRemitOrder.AgentRemitOrder;

/**
 * 代理提现订单 Service接口
 *
 * @author Administrator
 * @date 2020-04-02 22:24:19
 */
public interface IAgentRemitOrderService extends IService<AgentRemitOrder> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentRemitOrder agentRemitOrder
     * @return IPage<AgentRemitOrder>
     */
    IPage<AgentRemitOrder> findAgentRemitOrders(QueryRequest request, AgentRemitOrder agentRemitOrder, Date createStartTime, Date createEndTime);

    /**
     * 查询（所有）
     *
     * @param agentRemitOrder agentRemitOrder
     * @return List<AgentRemitOrder>
     */
    List<AgentRemitOrder> findAgentRemitOrders(AgentRemitOrder agentRemitOrder);

    /**
     * 新增
     *
     * @param agentRemitOrder agentRemitOrder
     */
    void createAgentRemitOrder(AgentRemitOrder agentRemitOrder,String payPassword);

    /**
     * 修改
     *
     * @param agentRemitOrder agentRemitOrder
     */
    void updateAgentRemitOrder(AgentRemitOrder agentRemitOrder);

    /**
     * 删除
     *
     * @param agentRemitOrder agentRemitOrder
     */
    void deleteAgentRemitOrder(AgentRemitOrder agentRemitOrder);

    /**
    * 删除
    *
    * @param agentRemitOrderIds agentRemitOrderIds
    */
    void deleteAgentRemitOrders(String[] agentRemitOrderIds);

    String getDataMd5(AgentRemitOrder agentRemitOrder);

    boolean checkDataMd5(AgentRemitOrder agentRemitOrder);

    /**
     * 审核通过
     * @param agentRemitOrder
     */
    void auditSuccess(AgentRemitOrder agentRemitOrder);

    /**
     * 审核不通过
     * @param id
     */
    void auditFail(Integer id);


    /**
     * 获取银行类型缓存
     * @return
     */
    List<Map<String,String>> getBanks();

    /**
     * 代理利润转额度
     * @param agent
     * @param remitAmount
     * @param payPassword
     */
    void transfer(Agent agent, Integer remitAmount, String payPassword);
}
