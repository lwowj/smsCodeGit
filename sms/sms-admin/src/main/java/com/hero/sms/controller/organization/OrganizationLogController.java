package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.organization.OrganizationLog;
import com.hero.sms.service.organization.IOrganizationLogService;
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
 * 商户操作日志表 Controller
 *
 * @author Administrator
 * @date 2020-03-21 23:29:41
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationLog")
public class OrganizationLogController extends BaseController {

    @Autowired
    private IOrganizationLogService organizationLogService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationLog")
    public String organizationLogIndex(){
        return FebsUtil.view("organizationLog/organizationLog");
    }

    @ControllerEndpoint(operation = "商户操作日志列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationLog:list")
    public FebsResponse getAllOrganizationLogs(OrganizationLog organizationLog) {
        return new FebsResponse().success().data(organizationLogService.findOrganizationLogs(organizationLog));
    }

    @ControllerEndpoint(operation = "商户操作日志列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationLog:list")
    public FebsResponse organizationLogList(QueryRequest request, OrganizationLog organizationLog, Date organizationLogStartTime, Date organizationLogEndTime) {
        Map<String, Object> dataTable = getDataTable(this.organizationLogService.findOrganizationLogs(request, organizationLog,organizationLogStartTime,organizationLogEndTime));
        return new FebsResponse().success().data(dataTable);
    }

/*    @ControllerEndpoint(operation = "新增OrganizationLog", exceptionMessage = "新增OrganizationLog失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationLog:add")
    public FebsResponse addOrganizationLog(@Valid OrganizationLog organizationLog) {
        this.organizationLogService.createOrganizationLog(organizationLog);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除OrganizationLog", exceptionMessage = "删除OrganizationLog失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organizationLog:delete")
    public FebsResponse deleteOrganizationLog(OrganizationLog organizationLog) {
        this.organizationLogService.deleteOrganizationLog(organizationLog);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除OrganizationLog", exceptionMessage = "批量删除OrganizationLog失败")
    @GetMapping("delete/{organizationLogIds}")
    @ResponseBody
    @RequiresPermissions("organizationLog:delete")
    public FebsResponse deleteOrganizationLog(@NotBlank(message = "{required}") @PathVariable String organizationLogIds) {
        String[] ids = organizationLogIds.split(StringPool.COMMA);
        this.organizationLogService.deleteOrganizationLogs(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改OrganizationLog", exceptionMessage = "修改OrganizationLog失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationLog:update")
    public FebsResponse updateOrganizationLog(OrganizationLog organizationLog) {
        this.organizationLogService.updateOrganizationLog(organizationLog);
        return new FebsResponse().success();
    }*/

    @ControllerEndpoint(operation = "修改OrganizationLog", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationLog:export")
    public void export(QueryRequest queryRequest, OrganizationLog organizationLog, HttpServletResponse response) {
        List<OrganizationLog> organizationLogs = this.organizationLogService.findOrganizationLogs(queryRequest, organizationLog,null,null).getRecords();
        ExcelKit.$Export(OrganizationLog.class, response).downXlsx(organizationLogs, false);
    }
}
