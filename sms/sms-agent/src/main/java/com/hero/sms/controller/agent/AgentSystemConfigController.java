package com.hero.sms.controller.agent;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.service.agent.IAgentSystemConfigService;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理系统配置 Controller
 *
 * @author Administrator
 * @date 2020-03-18 12:10:37
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentSystemConfig")
public class AgentSystemConfigController extends BaseController {

    @Autowired
    private IAgentSystemConfigService agentSystemConfigService;

    @ControllerEndpoint(operation = "代理系统配置")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:add")
    public FebsResponse addAgentSystemConfig(@Valid AgentSystemConfig agentSystemConfig) {
        try {
            agentSystemConfig.setAgentId(getCurrentAgent().getId());
            this.agentSystemConfigService.createAgentSystemConfig(agentSystemConfig,getCurrentAgent());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("代理系统配置失败",e);
            return new FebsResponse().message("代理系统配置失败").fail();
        }
        return new FebsResponse().success().message("设置成功");
    }



}
