package com.hero.sms.controller;

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
import com.hero.sms.service.organization.IOrganizationUserLimitService;

import lombok.extern.slf4j.Slf4j;

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

    @ControllerEndpoint( operation = "更新用户权限")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:update")
    public FebsResponse updateOrganizationUserLimits(String menuIds,Long userId) {
        try {
            this.organizationUserLimitService.updateeOrganizationUserLimits(menuIds,userId,getCurrentUser());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("更新用户权限失败",e);
            return new FebsResponse().message("更新用户权限失败").fail();
        }
        return new FebsResponse().success();
    }
}
