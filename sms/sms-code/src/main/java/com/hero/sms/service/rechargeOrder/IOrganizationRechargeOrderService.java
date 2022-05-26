package com.hero.sms.service.rechargeOrder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderExt;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderQuery;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 商户充值订单 Service接口
 *
 * @author Administrator
 * @date 2020-03-12 17:57:48
 */
public interface IOrganizationRechargeOrderService extends IService<OrganizationRechargeOrder> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationRechargeOrder organizationRechargeOrder
     * @return IPage<OrganizationRechargeOrder>
     */
    IPage<OrganizationRechargeOrder> findOrganizationRechargeOrders(QueryRequest request, OrganizationRechargeOrder organizationRechargeOrder, Date orgReqStartTime, Date orgReqEndTime);


    IPage<OrganizationRechargeOrderExt> extPage(QueryRequest request, OrganizationRechargeOrderQuery organizationRechargeOrderQuery);

    /**
     * 左联商户表查询
     * @param request
     * @param organizationRechargeOrder
     * @param agent
     * @return
     */
    IPage<OrganizationRechargeOrder> findOrganizationRechargeOrders(QueryRequest request, OrganizationRechargeOrder organizationRechargeOrder, Agent agent, Date orgReqStartTime, Date orgReqEndTime);

    List<StatisticBean> statisticBeanList(OrganizationRechargeOrderQuery organizationRechargeOrder);

    /**
     * 查询（所有）
     *
     * @param organizationRechargeOrder organizationRechargeOrder
     * @return List<OrganizationRechargeOrder>
     */
    List<OrganizationRechargeOrder> findOrganizationRechargeOrders(OrganizationRechargeOrder organizationRechargeOrder);

    /**
     * 新增
     *
     * @param organizationRechargeOrder organizationRechargeOrder
     */
    void createOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder);


    /**
     * 商户扣减额度
     * @param organizationRechargeOrder
     */
    void deducting(OrganizationRechargeOrder organizationRechargeOrder);

    /**
     * 代理端充值
     * @param organizationRechargeOrder
     * @param agent
     */
    @Transactional
    void createOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder, Agent agent,String payPassword);

    /**
     * 修改
     *
     * @param organizationRechargeOrder organizationRechargeOrder
     */
    void updateOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder);

    /**
     * 删除
     *
     * @param organizationRechargeOrder organizationRechargeOrder
     */
    void deleteOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder);

    /**
    * 删除
    *
    * @param organizationRechargeOrderIds organizationRechargeOrderIds
    */
    void deleteOrganizationRechargeOrders(String[] organizationRechargeOrderIds);
}
