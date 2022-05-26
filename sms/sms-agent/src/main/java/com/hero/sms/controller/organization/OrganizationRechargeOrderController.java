package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

/**
 * 商户充值订单 Controller
 *
 * @author Administrator
 * @date 2020-03-12 17:57:48
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationRechargeOrder")
public class OrganizationRechargeOrderController extends BaseController {

    @Autowired
    private IOrganizationRechargeOrderService organizationRechargeOrderService;
    @Autowired
    private IAgentService agentService;

    @ControllerEndpoint(operation = "商户充值订单列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:list")
    public FebsResponse organizationRechargeOrderList(QueryRequest request, OrganizationRechargeOrder organizationRechargeOrder, Date orgReqStartTime, Date orgReqEndTime) {
        Map<String, Object> dataTable = getDataTable(this.organizationRechargeOrderService.findOrganizationRechargeOrders(request, organizationRechargeOrder,getCurrentAgent(),orgReqStartTime,orgReqEndTime));
        dataTable.remove("payChannelId");
        dataTable.remove("payMerchNo");
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "商户充值")
    @PostMapping("add")
    @ResponseBody
    @Limit(key = "organizationRechargeOrder", period = 3, count = 1, name = "商户充值", prefix = "limit")
    @RequiresPermissions("organizationRechargeOrder:add")
    public FebsResponse addOrganizationRechargeOrder(@Valid OrganizationRechargeOrder organizationRechargeOrder,String verifyCode,String payPassword) {
        try {
            Agent agent = getCurrentAgent();
            agentService.checkGoogleKey(agent.getAgentAccount(),verifyCode);
            organizationRechargeOrder.setCeateUser(agent.getAgentName());
            this.organizationRechargeOrderService.createOrganizationRechargeOrder(organizationRechargeOrder,agent,payPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("商户充值失败",e);
            return new FebsResponse().message("商户充值失败").fail();
        }
        return new FebsResponse().success().data(organizationRechargeOrder);
    }

}
