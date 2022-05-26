package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @ControllerEndpoint(operation = "商户用户菜单关联")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("organizationUserLimit:update")
    public FebsResponse updateOrganizationUserLimits(String menuIds,Long userId) {
        this.organizationUserLimitService.updateeOrganizationUserLimits(menuIds,userId,getCurrentAgent());
        return new FebsResponse().success();
    }
}
