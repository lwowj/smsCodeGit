package com.hero.sms.controller.organization;


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
import com.hero.sms.entity.organization.OrganizationUserLoginLogQuery;
import com.hero.sms.service.organization.IOrganizationUserLoginLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户用户登录日志表 Controller
 *
 * @author Administrator
 * @date 2020-12-25 17:33:01
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationUserLoginLog")
public class OrganizationUserLoginLogController extends BaseController {

    @Autowired
    private IOrganizationUserLoginLogService organizationUserLoginLogService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUserLoginLog")
    public String organizationUserLoginLogIndex(){
        return FebsUtil.view("organizationUserLoginLog/organizationUserLoginLog");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationUserLoginLog:list")
    public FebsResponse getAllOrganizationUserLoginLogs(OrganizationUserLoginLogQuery organizationUserLoginLog) 
    {
    	String getOrganizationCode = organizationUserLoginLog.getOrganizationCode();
    	if(StringUtils.isNotBlank(getOrganizationCode))
 	    {
    		organizationUserLoginLog.setOrganizationCode(getOrganizationCode.trim());
 	    }
    	
    	String getUserAccount = organizationUserLoginLog.getUserAccount();
    	if(StringUtils.isNotBlank(getUserAccount))
 	    {
    		organizationUserLoginLog.setUserAccount(getUserAccount.trim());
 	    }
    	
    	String getUserAccountFuzzy = organizationUserLoginLog.getUserAccountFuzzy();
    	if(StringUtils.isNotBlank(getUserAccountFuzzy))
 	    {
    		organizationUserLoginLog.setUserAccountFuzzy(getUserAccountFuzzy.trim());
 	    }
    	
    	String getLocalIp = organizationUserLoginLog.getLocalIp();
    	if(StringUtils.isNotBlank(getLocalIp))
 	    {
    		organizationUserLoginLog.setLocalIp(getLocalIp.trim());
 	    }
    	
    	String getLocalIpFuzzy = organizationUserLoginLog.getLocalIpFuzzy();
    	if(StringUtils.isNotBlank(getLocalIpFuzzy))
 	    {
    		organizationUserLoginLog.setLocalIpFuzzy(getLocalIpFuzzy.trim());
 	    }
    	
    	String getMessage = organizationUserLoginLog.getMessage();
    	if(StringUtils.isNotBlank(getMessage))
 	    {
    		organizationUserLoginLog.setMessage(getMessage.trim());
 	    }
        return new FebsResponse().success().data(organizationUserLoginLogService.findOrganizationUserLoginLogs(organizationUserLoginLog));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationUserLoginLog:view")
    public FebsResponse organizationUserLoginLogList(QueryRequest request, OrganizationUserLoginLogQuery organizationUserLoginLog) 
    {
    	String getOrganizationCode = organizationUserLoginLog.getOrganizationCode();
    	if(StringUtils.isNotBlank(getOrganizationCode))
 	    {
    		organizationUserLoginLog.setOrganizationCode(getOrganizationCode.trim());
 	    }
    	
    	String getUserAccount = organizationUserLoginLog.getUserAccount();
    	if(StringUtils.isNotBlank(getUserAccount))
 	    {
    		organizationUserLoginLog.setUserAccount(getUserAccount.trim());
 	    }
    	
    	String getUserAccountFuzzy = organizationUserLoginLog.getUserAccountFuzzy();
    	if(StringUtils.isNotBlank(getUserAccountFuzzy))
 	    {
    		organizationUserLoginLog.setUserAccountFuzzy(getUserAccountFuzzy.trim());
 	    }
    	
    	String getLocalIp = organizationUserLoginLog.getLocalIp();
    	if(StringUtils.isNotBlank(getLocalIp))
 	    {
    		organizationUserLoginLog.setLocalIp(getLocalIp.trim());
 	    }
    	
    	String getLocalIpFuzzy = organizationUserLoginLog.getLocalIpFuzzy();
    	if(StringUtils.isNotBlank(getLocalIpFuzzy))
 	    {
    		organizationUserLoginLog.setLocalIpFuzzy(getLocalIpFuzzy.trim());
 	    }
    	
    	String getMessage = organizationUserLoginLog.getMessage();
    	if(StringUtils.isNotBlank(getMessage))
 	    {
    		organizationUserLoginLog.setMessage(getMessage.trim());
 	    }
        Map<String, Object> dataTable = getDataTable(this.organizationUserLoginLogService.findOrganizationUserLoginLogs(request, organizationUserLoginLog));
        return new FebsResponse().success().data(dataTable);
    }

//    @ControllerEndpoint(operation = "新增OrganizationUserLoginLog", exceptionMessage = "新增OrganizationUserLoginLog失败")
//    @PostMapping
//    @ResponseBody
//    @RequiresPermissions("organizationUserLoginLog:add")
//    public FebsResponse addOrganizationUserLoginLog(@Valid OrganizationUserLoginLog organizationUserLoginLog) {
//        this.organizationUserLoginLogService.createOrganizationUserLoginLog(organizationUserLoginLog);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "删除OrganizationUserLoginLog", exceptionMessage = "删除OrganizationUserLoginLog失败")
//    @GetMapping("delete")
//    @ResponseBody
//    @RequiresPermissions("organizationUserLoginLog:delete")
//    public FebsResponse deleteOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog) {
//        this.organizationUserLoginLogService.deleteOrganizationUserLoginLog(organizationUserLoginLog);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "批量删除OrganizationUserLoginLog", exceptionMessage = "批量删除OrganizationUserLoginLog失败")
//    @GetMapping("delete/{organizationUserLoginLogIds}")
//    @ResponseBody
//    @RequiresPermissions("organizationUserLoginLog:delete")
//    public FebsResponse deleteOrganizationUserLoginLog(@NotBlank(message = "{required}") @PathVariable String organizationUserLoginLogIds) {
//        String[] ids = organizationUserLoginLogIds.split(StringPool.COMMA);
//        this.organizationUserLoginLogService.deleteOrganizationUserLoginLogs(ids);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "修改OrganizationUserLoginLog", exceptionMessage = "修改OrganizationUserLoginLog失败")
//    @PostMapping("update")
//    @ResponseBody
//    @RequiresPermissions("organizationUserLoginLog:update")
//    public FebsResponse updateOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog) {
//        this.organizationUserLoginLogService.updateOrganizationUserLoginLog(organizationUserLoginLog);
//        return new FebsResponse().success();
//    }
//
//    @ControllerEndpoint(operation = "修改OrganizationUserLoginLog", exceptionMessage = "导出Excel失败")
//    @PostMapping("excel")
//    @ResponseBody
//    @RequiresPermissions("organizationUserLoginLog:export")
//    public void export(QueryRequest queryRequest, OrganizationUserLoginLog organizationUserLoginLog, HttpServletResponse response) {
//        List<OrganizationUserLoginLog> organizationUserLoginLogs = this.organizationUserLoginLogService.findOrganizationUserLoginLogs(queryRequest, organizationUserLoginLog).getRecords();
//        ExcelKit.$Export(OrganizationUserLoginLog.class, response).downXlsx(organizationUserLoginLogs, false);
//    }
}
