package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.system.entity.User;
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
 * 商户用户资费 Controller
 *
 * @author Administrator
 * @date 2020-03-08 00:12:30
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationCost")
public class OrganizationCostController extends BaseController {

    @Autowired
    private IOrganizationCostService organizationCostService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationCost")
    public String organizationCostIndex(){
        return FebsUtil.view("organizationCost/organizationCost");
    }

    @ControllerEndpoint(operation = "商户用户资费列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationCost:view")
    public FebsResponse getAllOrganizationCosts(OrganizationCost organizationCost) {
        return new FebsResponse().success().data(organizationCostService.findOrganizationCosts(organizationCost));
    }

    @ControllerEndpoint(operation = "商户用户资费列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationCost:view")
    public FebsResponse organizationCostList(QueryRequest request, OrganizationCost organizationCost) {
        Map<String, Object> dataTable = getDataTable(this.organizationCostService.findOrganizationCosts(request, organizationCost));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "商户用户资费新增")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationCost:add")
    public FebsResponse addOrganizationCost(@Valid OrganizationCost organizationCost) {
        this.organizationCostService.createOrganizationCost(organizationCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户资费删除")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organizationCost:delete")
    public FebsResponse deleteOrganizationCost(OrganizationCost organizationCost) {
        this.organizationCostService.deleteOrganizationCost(organizationCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户资费删除")
    @GetMapping("delete/{organizationCostIds}")
    @ResponseBody
    @RequiresPermissions("organizationCost:delete")
    public FebsResponse deleteOrganizationCost(@NotBlank(message = "{required}") @PathVariable String organizationCostIds) {
        String[] ids = organizationCostIds.split(StringPool.COMMA);
        this.organizationCostService.deleteOrganizationCosts(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户资费更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationCost:update")
    public FebsResponse updateOrganizationCost(OrganizationCost organizationCost) {
        this.organizationCostService.updateOrganizationCost(organizationCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户资费更新")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("organizationCost:updates")
    public FebsResponse updateOrganizationCosts(@RequestBody List<OrganizationCost> organizationCosts) {
        try {
            User user = super.getCurrentUser();
            this.organizationCostService.updateOrganizationCosts(organizationCosts,user.getUsername());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("商户用户资费更新失败",e);
            return new FebsResponse().message("商户用户资费更新失败").fail();
        }
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationCost:export")
    public void export(QueryRequest queryRequest, OrganizationCost organizationCost, HttpServletResponse response) {
        List<OrganizationCost> organizationCosts = this.organizationCostService.findOrganizationCosts(queryRequest, organizationCost).getRecords();
        ExcelKit.$Export(OrganizationCost.class, response).downXlsx(organizationCosts, false);
    }
}
