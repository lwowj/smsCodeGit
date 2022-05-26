package com.hero.sms.controller.agent;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.AgentLoginLogQuery;
import com.hero.sms.service.agent.IAgentLoginLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理登录日志表 Controller
 *
 * @author Administrator
 * @date 2020-12-25 17:32:55
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentLoginLog")
public class AgentLoginLogController extends BaseController {

    @Autowired
    private IAgentLoginLogService agentLoginLogService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentLoginLog")
    public String agentLoginLogIndex(){
        return FebsUtil.view("agentLoginLog/agentLoginLog");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("agentLoginLog:list")
    public FebsResponse getAllAgentLoginLogs(AgentLoginLogQuery agentLoginLog) {
    	String agentAccount = agentLoginLog.getAgentAccount();
    	if(StringUtils.isNotBlank(agentAccount))
 	    {
    		agentLoginLog.setAgentAccount(agentAccount.trim());
 	    }
    	String agentAccountFuzzy = agentLoginLog.getAgentAccountFuzzy();
    	if(StringUtils.isNotBlank(agentAccountFuzzy))
 	    {
    		agentLoginLog.setAgentAccountFuzzy(agentAccountFuzzy.trim());
 	    }
    	String getLocalIp = agentLoginLog.getLocalIp();
    	if(StringUtils.isNotBlank(getLocalIp))
 	    {
    		agentLoginLog.setLocalIp(getLocalIp.trim());
 	    }
    	String getLocalIpFuzzy = agentLoginLog.getLocalIpFuzzy();
    	if(StringUtils.isNotBlank(getLocalIpFuzzy))
 	    {
    		agentLoginLog.setLocalIpFuzzy(getLocalIpFuzzy.trim());
 	    }
    	String getMessage = agentLoginLog.getMessage();
    	if(StringUtils.isNotBlank(getMessage))
 	    {
    		agentLoginLog.setMessage(getMessage.trim());
 	    }
        return new FebsResponse().success().data(agentLoginLogService.findAgentLoginLogs(agentLoginLog));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentLoginLog:view")
    public FebsResponse agentLoginLogList(QueryRequest request, AgentLoginLogQuery agentLoginLog) {
    	String agentAccount = agentLoginLog.getAgentAccount();
    	if(StringUtils.isNotBlank(agentAccount))
 	    {
    		agentLoginLog.setAgentAccount(agentAccount.trim());
 	    }
    	String agentAccountFuzzy = agentLoginLog.getAgentAccountFuzzy();
    	if(StringUtils.isNotBlank(agentAccountFuzzy))
 	    {
    		agentLoginLog.setAgentAccountFuzzy(agentAccountFuzzy.trim());
 	    }
    	String getLocalIp = agentLoginLog.getLocalIp();
    	if(StringUtils.isNotBlank(getLocalIp))
 	    {
    		agentLoginLog.setLocalIp(getLocalIp.trim());
 	    }
    	String getLocalIpFuzzy = agentLoginLog.getLocalIpFuzzy();
    	if(StringUtils.isNotBlank(getLocalIpFuzzy))
 	    {
    		agentLoginLog.setLocalIpFuzzy(getLocalIpFuzzy.trim());
 	    }
    	String getMessage = agentLoginLog.getMessage();
    	if(StringUtils.isNotBlank(getMessage))
 	    {
    		agentLoginLog.setMessage(getMessage.trim());
 	    }
        Map<String, Object> dataTable = getDataTable(this.agentLoginLogService.findAgentLoginLogs(request, agentLoginLog));
        return new FebsResponse().success().data(dataTable);
    }

//    @ControllerEndpoint(operation = "新增AgentLoginLog", exceptionMessage = "新增AgentLoginLog失败")
//    @PostMapping
//    @ResponseBody
//    @RequiresPermissions("agentLoginLog:add")
//    public FebsResponse addAgentLoginLog(@Valid AgentLoginLog agentLoginLog) {
//        this.agentLoginLogService.createAgentLoginLog(agentLoginLog);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "删除AgentLoginLog", exceptionMessage = "删除AgentLoginLog失败")
//    @GetMapping("delete")
//    @ResponseBody
//    @RequiresPermissions("agentLoginLog:delete")
//    public FebsResponse deleteAgentLoginLog(AgentLoginLog agentLoginLog) {
//        this.agentLoginLogService.deleteAgentLoginLog(agentLoginLog);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "批量删除AgentLoginLog", exceptionMessage = "批量删除AgentLoginLog失败")
//    @GetMapping("delete/{agentLoginLogIds}")
//    @ResponseBody
//    @RequiresPermissions("agentLoginLog:delete")
//    public FebsResponse deleteAgentLoginLog(@NotBlank(message = "{required}") @PathVariable String agentLoginLogIds) {
//        String[] ids = agentLoginLogIds.split(StringPool.COMMA);
//        this.agentLoginLogService.deleteAgentLoginLogs(ids);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "修改AgentLoginLog", exceptionMessage = "修改AgentLoginLog失败")
//    @PostMapping("update")
//    @ResponseBody
//    @RequiresPermissions("agentLoginLog:update")
//    public FebsResponse updateAgentLoginLog(AgentLoginLog agentLoginLog) {
//        this.agentLoginLogService.updateAgentLoginLog(agentLoginLog);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "修改AgentLoginLog", exceptionMessage = "导出Excel失败")
//    @PostMapping("excel")
//    @ResponseBody
//    @RequiresPermissions("agentLoginLog:export")
//    public void export(QueryRequest queryRequest, AgentLoginLog agentLoginLog, HttpServletResponse response) {
//        List<AgentLoginLog> agentLoginLogs = this.agentLoginLogService.findAgentLoginLogs(queryRequest, agentLoginLog).getRecords();
//        ExcelKit.$Export(AgentLoginLog.class, response).downXlsx(agentLoginLogs, false);
//    }
}
