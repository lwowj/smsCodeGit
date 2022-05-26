package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.AgentMenuLimit;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentMenuLimit")
    public String agentMenuLimitIndex(){
        return FebsUtil.view("agentMenuLimit/agentMenuLimit");
    }

    @ControllerEndpoint(operation = "代理菜单关联列表")
    @GetMapping("agentMenuLimit")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:list")
    public FebsResponse getAllAgentMenuLimits(AgentMenuLimit agentMenuLimit) {
        return new FebsResponse().success().data(agentMenuLimitService.findAgentMenuLimits(agentMenuLimit));
    }

    @ControllerEndpoint(operation = "代理菜单关联列表")
    @GetMapping("agentMenuLimit/list")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:list")
    public FebsResponse agentMenuLimitList(QueryRequest request, AgentMenuLimit agentMenuLimit) {
        Map<String, Object> dataTable = getDataTable(this.agentMenuLimitService.findAgentMenuLimits(request, agentMenuLimit));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "代理菜单关联新增")
    @PostMapping("agentMenuLimit")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:add")
    public FebsResponse addAgentMenuLimit(@Valid AgentMenuLimit agentMenuLimit) {
        this.agentMenuLimitService.createAgentMenuLimit(agentMenuLimit);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理菜单关联删除")
    @GetMapping("agentMenuLimit/delete")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:delete")
    public FebsResponse deleteAgentMenuLimit(AgentMenuLimit agentMenuLimit) {
        this.agentMenuLimitService.deleteAgentMenuLimit(agentMenuLimit);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理菜单关联更新")
    @PostMapping("agentMenuLimit/update")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:update")
    public FebsResponse updateAgentMenuLimit(AgentMenuLimit agentMenuLimit) {
        this.agentMenuLimitService.updateAgentMenuLimit(agentMenuLimit);
        return new FebsResponse().success();
    }

    @PostMapping("agentMenuLimit/excel")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:export")
    public void export(QueryRequest queryRequest, AgentMenuLimit agentMenuLimit, HttpServletResponse response) {
        List<AgentMenuLimit> agentMenuLimits = this.agentMenuLimitService.findAgentMenuLimits(queryRequest, agentMenuLimit).getRecords();
        ExcelKit.$Export(AgentMenuLimit.class, response).downXlsx(agentMenuLimits, false);
    }

    @ControllerEndpoint(operation = "代理菜单关联更新")
    @PostMapping("agentMenuLimit/updates")
    @ResponseBody
    @RequiresPermissions("agentMenuLimit:update")
    public FebsResponse updateAgentMenuLimits(String menuIds,Long agentId) {
        this.agentMenuLimitService.updateAgentMenuLimits(menuIds,agentId);
        return new FebsResponse().success();
    }
}
