package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.service.agent.IAgentCostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 商户代理资费 Controller
 *
 * @author Administrator
 * @date 2020-03-06 10:05:33
 */
@Slf4j
@Validated
@Controller
public class AgentCostController extends BaseController {

    @Autowired
    private IAgentCostService agentCostService;

    @ControllerEndpoint(operation = "更新代理资费")
    @PostMapping("agentCost/updates")
    @ResponseBody
    @RequiresPermissions("agentCost:updates")
    public FebsResponse updateAgentCosts(@RequestBody List<AgentCost> agentCosts) {
        try {
            Agent agent = super.getCurrentAgent();
            this.agentCostService.updateAgentCosts(agentCosts,agent);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("更新代理资费失败",e);
            return new FebsResponse().message("更新代理资费失败").fail();
        }
        return new FebsResponse().success();
    }

}
