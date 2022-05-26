package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 商户用户菜单关联表 Controller
 *
 * @author Administrator
 * @date 2020-03-08 00:13:33
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationUserLimit")
public class OrganizationUserLimitController extends BaseController {

    @Autowired
    private IOrganizationUserLimitService organizationUserLimitService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUserLimit")
    public String organizationUserLimitIndex(){
        return FebsUtil.view("organizationUserLimit/organizationUserLimit");
    }

    @ControllerEndpoint(operation = "商户用户菜单关联列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:view")
    public FebsResponse getAllOrganizationUserLimits(OrganizationUserLimit organizationUserLimit) {
        return new FebsResponse().success().data(organizationUserLimitService.findOrganizationUserLimits(organizationUserLimit));
    }

    @ControllerEndpoint(operation = "商户用户菜单关联列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:view")
    public FebsResponse organizationUserLimitList(QueryRequest request, OrganizationUserLimit organizationUserLimit) {
        Map<String, Object> dataTable = getDataTable(this.organizationUserLimitService.findOrganizationUserLimits(request, organizationUserLimit));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "商户用户菜单关联新增")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:add")
    public FebsResponse addOrganizationUserLimit(@Valid OrganizationUserLimit organizationUserLimit) {
        this.organizationUserLimitService.createOrganizationUserLimit(organizationUserLimit);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户菜单关联删除")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:delete")
    public FebsResponse deleteOrganizationUserLimit(OrganizationUserLimit organizationUserLimit) {
        this.organizationUserLimitService.deleteOrganizationUserLimit(organizationUserLimit);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户菜单关联删除")
    @GetMapping("delete/{organizationUserLimitIds}")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:delete")
    public FebsResponse deleteOrganizationUserLimit(@NotBlank(message = "{required}") @PathVariable String organizationUserLimitIds) {
        String[] ids = organizationUserLimitIds.split(StringPool.COMMA);
        this.organizationUserLimitService.deleteOrganizationUserLimits(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户菜单关联更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:update")
    public FebsResponse updateOrganizationUserLimit(OrganizationUserLimit organizationUserLimit) {
        this.organizationUserLimitService.updateOrganizationUserLimit(organizationUserLimit);
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:export")
    public void export(QueryRequest queryRequest, OrganizationUserLimit organizationUserLimit, HttpServletResponse response) {
        List<OrganizationUserLimit> organizationUserLimits = this.organizationUserLimitService.findOrganizationUserLimits(queryRequest, organizationUserLimit).getRecords();
        ExcelKit.$Export(OrganizationUserLimit.class, response).downXlsx(organizationUserLimits, false);
    }

    @ControllerEndpoint(operation = "商户用户菜单关联更新")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:update")
    public FebsResponse updateOrganizationUserLimits(String menuIds,Long userId) {
        this.organizationUserLimitService.updateeOrganizationUserLimits(menuIds,userId);
        return new FebsResponse().success();
    }
}
