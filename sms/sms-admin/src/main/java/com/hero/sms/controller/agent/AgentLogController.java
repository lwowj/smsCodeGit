package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.AgentLog;
import com.hero.sms.service.agent.IAgentLogService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 代理操作日志表 Controller
 *
 * @author Administrator
 * @date 2020-04-02 11:01:33
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentLog")
public class AgentLogController extends BaseController {

    @Autowired
    private IAgentLogService agentLogService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentLog")
    public String agentLogIndex(){
        return FebsUtil.view("agentLog/agentLog");
    }

    @ControllerEndpoint(operation = "代理操作日志列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("agentLog:list")
    public FebsResponse getAllAgentLogs(AgentLog agentLog) {
        return new FebsResponse().success().data(agentLogService.findAgentLogs(agentLog));
    }

    @ControllerEndpoint(operation = "代理操作日志列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentLog:list")
    public FebsResponse agentLogList(QueryRequest request, AgentLog agentLog, Date agentLogStartTime, Date agentLogEndTime) {
        Map<String, Object> dataTable = getDataTable(this.agentLogService.findAgentLogs(request, agentLog,agentLogStartTime,agentLogEndTime));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "修改AgentLog", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("agentLog:export")
    public void export(QueryRequest queryRequest, AgentLog agentLog, HttpServletResponse response) {
        List<AgentLog> agentLogs = this.agentLogService.findAgentLogs(queryRequest, agentLog,null,null).getRecords();
        ExcelKit.$Export(AgentLog.class, response).downXlsx(agentLogs, false);
    }
}
