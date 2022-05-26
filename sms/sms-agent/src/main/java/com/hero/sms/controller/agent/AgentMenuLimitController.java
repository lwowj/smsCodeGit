package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 代理菜单关联表 Controller
 *
 * @author Administrator
 * @date 2020-03-06 10:05:54
 */
@Slf4j
@Validated
@Controller
public class AgentMenuLimitController extends BaseController {

    @Autowired
    private IAgentMenuLimitService agentMenuLimitService;

    @ControllerEndpoint(operation = "更新代理菜单关联")
    @PostMapping("agentMenuLimit/updates")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:update")
    public FebsResponse updateAgentMenuLimits(String menuIds,Long agentId) {
        try {
            this.agentMenuLimitService.updateAgentMenuLimits(menuIds,agentId,getCurrentAgent());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("更新代理菜单关联失败",e);
            return new FebsResponse().message("更新代理菜单关联失败").fail();
        }
        return new FebsResponse().success();
    }
}
