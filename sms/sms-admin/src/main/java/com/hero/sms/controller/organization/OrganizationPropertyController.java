package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.service.organization.IOrganizationPropertyService;
import com.hero.sms.system.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Validated
@Controller
@RequestMapping("organizationProperty")
public class OrganizationPropertyController extends BaseController {

    @Autowired
    private IOrganizationPropertyService organizationPropertyService;

    @ControllerEndpoint(operation = "修改organizationProperty", exceptionMessage = "修改organizationProperty失败")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("organizationProperty:update")
    public FebsResponse updateSmsChannelProperties(@RequestBody List<OrganizationProperty> organizationProperties) {
        User user = super.getCurrentUser();
        this.organizationPropertyService.updateSmsChannelProperties(organizationProperties,user.getUsername());
        return new FebsResponse().success();
    }
}
