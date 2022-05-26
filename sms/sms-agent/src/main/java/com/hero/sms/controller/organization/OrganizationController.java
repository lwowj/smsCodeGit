package com.hero.sms.controller.organization;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ISessionService;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.organization.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 商户信息 Controller
 *
 * @author Administrator
 * @date 2020-03-07 17:24:55
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organization")
public class OrganizationController extends BaseController {

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private ISessionService sessionService;

    @ControllerEndpoint(operation = "商户列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organization:view")
    public FebsResponse organizationList(QueryRequest request, Organization organization) {
        Agent agent = getCurrentAgent();
        organization.setAgentId(agent.getId());
        Map<String, Object> dataTable = getDataTable(this.organizationService.findOrganizations(request, organization));
        dataTable.remove("smsSign");
        dataTable.remove("md5Key");
        dataTable.remove("bindIp");
        dataTable.remove("notifyUrl");
        dataTable.remove("dataMd5");
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增商户")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organization:add")
    public FebsResponse addOrganization(@Valid Organization organization,
                @NotBlank(message = "{required}") String userAccount,
                @NotBlank(message = "{required}") String userPassword) {
        try {
            Agent agent = super.getCurrentAgent();
            organization.setAgentId(agent.getId());
            organization.setCreateUser(agent.getAgentAccount());
            this.organizationService.createOrganization(organization,userAccount,userPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("新增商户失败",e);
            return new FebsResponse().message("新增商户失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户锁定")
    @GetMapping("lock/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:lock")
    public FebsResponse lockOrganization(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] orgIds = organizationIds.split(StringPool.COMMA);
        Agent agent = super.getCurrentAgent();
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getStatus, OrgStatusEnums.Lock.getCode())
                .eq(Organization::getStatus,OrgStatusEnums.Normal.getCode())
                .eq(Organization::getAgentId,agent.getId())
                .in(Organization::getId,orgIds);
        this.organizationService.update(wrapper);
        try {
            sessionService.forceLogoutOrg(organizationIds,agent.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户解锁")
    @GetMapping("unlock/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:unlock")
    public FebsResponse unlockOrganization(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] orgIds = organizationIds.split(StringPool.COMMA);
        Agent agent = super.getCurrentAgent();
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getStatus, OrgStatusEnums.Normal.getCode())
                .eq(Organization::getStatus,OrgStatusEnums.Lock.getCode())
                .eq(Organization::getAgentId,agent.getId())
                .in(Organization::getId,orgIds);
        this.organizationService.update(wrapper);
        return new FebsResponse().success();
    }


    @ControllerEndpoint(operation = "更新商户")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organization:update")
    public FebsResponse updateOrganization(Organization organization) {
        try {
            this.organizationService.updateOrganization(organization,getCurrentAgent().getId());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("更新商户失败",e);
            return new FebsResponse().message("更新商户失败").fail();
        }
        return new FebsResponse().success();
    }

    @GetMapping("getOrgs")
    @ResponseBody
    public FebsResponse getOrgs(){
        List<Map<String,String>> list = this.organizationService.getOrgsByAgentId(getCurrentAgent().getId());
        return new FebsResponse().success().data(list);
    }
}
