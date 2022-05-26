package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.rechargeOrder.IAgentRechargeOrderService;
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
 * 代理充值订单 Controller
 *
 * @author Administrator
 * @date 2020-03-12 18:00:59
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentRechargeOrder")
public class AgentRechargeOrderController extends BaseController {

    @Autowired
    private IAgentRechargeOrderService agentRechargeOrderService;
    @Autowired
    private IAgentService agentService;

    @ControllerEndpoint(operation = "代理充值订单列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:list")
    public FebsResponse agentRechargeOrderList(QueryRequest request, AgentRechargeOrder agentRechargeOrder,Date agentReqStartTime, Date agentReqEndTime) {
        agentRechargeOrder.setAgentId(getCurrentAgent().getId() + "");
        Map<String, Object> dataTable = getDataTable(this.agentRechargeOrderService.findAgentRechargeOrders(request, agentRechargeOrder,agentReqStartTime,agentReqEndTime,getCurrentAgent()));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "代理充值")
    @Limit(key = "agentRechargeOrder", period = 3, count = 1, name = "代理充值", prefix = "limit")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:add")
    public FebsResponse addAgentRechargeOrder(@Valid AgentRechargeOrder agentRechargeOrder,String verifyCode,String payPassword) {
        try {
            Agent agent = super.getCurrentAgent();
            agentService.checkGoogleKey(agent.getAgentAccount(), verifyCode);
            agentRechargeOrder.setCeateUser(agent.getAgentName());
            this.agentRechargeOrderService.createAgentRechargeOrder(agentRechargeOrder,getCurrentAgent(),payPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("代理充值失败",e);
            return new FebsResponse().message("代理充值失败").fail();
        }
        return new FebsResponse().success().data(agentRechargeOrder);
    }

}
