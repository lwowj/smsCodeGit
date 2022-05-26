package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationGroup;
import com.hero.sms.service.organization.IOrganizationGroupService;
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
 * 商户分组表 Controller
 *
 * @author Administrator
 * @date 2020-06-20 22:38:28
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationGroup")
public class OrganizationGroupController extends BaseController {

    @Autowired
    private IOrganizationGroupService organizationGroupService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationGroup")
    public String organizationGroupIndex(){
        return FebsUtil.view("organizationGroup/organizationGroup");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationGroup:list")
    public FebsResponse getAllOrganizationGroups(OrganizationGroup organizationGroup) {
        return new FebsResponse().success().data(organizationGroupService.findOrganizationGroups(organizationGroup));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationGroup:list")
    public FebsResponse organizationGroupList(QueryRequest request, OrganizationGroup organizationGroup) {
        Map<String, Object> dataTable = getDataTable(this.organizationGroupService.findOrganizationGroups(request, organizationGroup));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增OrganizationGroup", exceptionMessage = "新增OrganizationGroup失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationGroup:add")
    public FebsResponse addOrganizationGroup(@Valid OrganizationGroup organizationGroup) {
        this.organizationGroupService.createOrganizationGroup(organizationGroup);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "编辑商户分组", exceptionMessage = "编辑商户分组失败")
    @PostMapping("incrementSave")
    @ResponseBody
    @RequiresPermissions("organization:updateGroup")
    public FebsResponse incrementSaveOrganizationGroup(String orgCode,String groupIds) {
        try {
            this.organizationGroupService.incrementSaveOrganizationGroup(orgCode,groupIds);
        } catch (ServiceException e) {
            new FebsResponse().success().message(e.getMessage());
        }
        return new FebsResponse().success().message("数据更新成功");
    }

    @ControllerEndpoint(operation = "删除OrganizationGroup", exceptionMessage = "删除OrganizationGroup失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organizationGroup:delete")
    public FebsResponse deleteOrganizationGroup(OrganizationGroup organizationGroup) {
        this.organizationGroupService.deleteOrganizationGroup(organizationGroup);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除OrganizationGroup", exceptionMessage = "批量删除OrganizationGroup失败")
    @GetMapping("delete/{organizationGroupIds}")
    @ResponseBody
    @RequiresPermissions("organizationGroup:delete")
    public FebsResponse deleteOrganizationGroup(@NotBlank(message = "{required}") @PathVariable String organizationGroupIds) {
        String[] ids = organizationGroupIds.split(StringPool.COMMA);
        this.organizationGroupService.deleteOrganizationGroups(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改OrganizationGroup", exceptionMessage = "修改OrganizationGroup失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationGroup:update")
    public FebsResponse updateOrganizationGroup(OrganizationGroup organizationGroup) {
        this.organizationGroupService.updateOrganizationGroup(organizationGroup);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改OrganizationGroup", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationGroup:export")
    public void export(QueryRequest queryRequest, OrganizationGroup organizationGroup, HttpServletResponse response) {
        List<OrganizationGroup> organizationGroups = this.organizationGroupService.findOrganizationGroups(queryRequest, organizationGroup).getRecords();
        ExcelKit.$Export(OrganizationGroup.class, response).downXlsx(organizationGroups, false);
    }
}
