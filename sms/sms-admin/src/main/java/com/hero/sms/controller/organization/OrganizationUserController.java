package com.hero.sms.controller.organization;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.monitor.service.ISessionService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户用户 Controller
 *
 * @author Administrator
 * @date 2020-03-07 21:36:18
 */
@Slf4j
@Validated
@RestController
@RequestMapping("organizationUser")
public class OrganizationUserController extends BaseController {

    @Autowired
    private IOrganizationUserService organizationUserService;

    @Autowired
    private ISessionService sessionService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser")
    public String organizationUserIndex(){
        return FebsUtil.view("organizationUser/organizationUser");
    }

    @GetMapping("check/{userAccount}")
    public boolean checkUserAccount(@NotBlank(message = "{required}") @PathVariable String userAccount, String userId){
        OrganizationUser orgUser = this.organizationUserService.findByUserAccount(userAccount);
        return orgUser==null || StringUtils.isNotBlank(userId);
    }

    @ControllerEndpoint(operation = "商户用户列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationUser:view")
    public FebsResponse getAllOrganizationUsers(OrganizationUser organizationUser) {
        return new FebsResponse().success().data(organizationUserService.findOrganizationUsers(organizationUser));
    }

    @ControllerEndpoint(operation = "商户用户列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationUser:view")
    public FebsResponse organizationUserList(QueryRequest request, OrganizationUser organizationUser) {
        Map<String, Object> dataTable = getDataTable(this.organizationUserService.findOrganizationUsers(request, organizationUser));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "商户用户新增")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationUser:add")
    public FebsResponse addOrganizationUser(@Valid OrganizationUser organizationUser) {
        try {
            organizationUser.setCreateUser(getCurrentUser().getUsername());
            organizationUser.setCreateDate(new Date());
            this.organizationUserService.createOrganizationUser(organizationUser);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("商户用户新增失败",e);
            return new FebsResponse().message("商户用户新增失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户删除")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organizationUser:delete")
    public FebsResponse deleteOrganizationUser(OrganizationUser organizationUser) {
        this.organizationUserService.deleteOrganizationUser(organizationUser);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户删除")
    @GetMapping("delete/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:delete")
    public FebsResponse deleteOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.deleteOrganizationUsers(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationUser:update")
    public FebsResponse updateOrganizationUser(OrganizationUser organizationUser) {
        try {
            this.organizationUserService.updateOrganizationUser(organizationUser);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("商户用户更新失败",e);
            return new FebsResponse().message("商户用户更新失败").fail();
        }
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationUser:export")
    public void export(QueryRequest queryRequest, OrganizationUser organizationUser, HttpServletResponse response) {
        List<OrganizationUser> organizationUsers = this.organizationUserService.findOrganizationUsers(queryRequest, organizationUser).getRecords();
        ExcelKit.$Export(OrganizationUser.class, response).downXlsx(organizationUsers, false);
    }

    @ControllerEndpoint(operation = "商户用户解锁")
    @GetMapping("unlock/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:unlock")
    public FebsResponse unlockOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.updateUserStatus(ids,"1");
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户锁定")
    @GetMapping("lock/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:lock")
    public FebsResponse lockOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.updateUserStatus(ids,"0");
        sessionService.forceLogoutOrgUser(organizationUserIds);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户非强制绑定")
    @GetMapping("optional/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:optionalGoogle")
    public FebsResponse optionalOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.updateUserNeedGoogleStatus(ids,"0");
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户强制绑定")
    @GetMapping("mandatory/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:mandatoryGoogle")
    public FebsResponse mandatoryOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.updateUserNeedGoogleStatus(ids,"1");
        return new FebsResponse().success();
    }
    
    @PostMapping("password/reset/{userAccounts}")
    @RequiresPermissions("organizationUser:password:reset")
    @ControllerEndpoint(operation = "重置用户密码",exceptionMessage = "重置用户密码失败")
    public FebsResponse resetPassword(@NotBlank(message = "{required}") @PathVariable String userAccounts) {
        String[] accounts = userAccounts.split(StringPool.COMMA);
        this.organizationUserService.resetPassword(accounts);
        return new FebsResponse().success();
    }
    
    /**
     * 重置商户用户Google口令（设置为NULL）
     */
    @PostMapping("googleKey/reset/{userAccounts}")
    @RequiresPermissions("organizationUser:googleKey:reset")
    @ControllerEndpoint(operation = "重置用户Google口令",exceptionMessage = "重置用户Google口令失败")
    public FebsResponse resetGoogleKey(@NotBlank(message = "{required}") @PathVariable String userAccounts) {
        String[] accounts = userAccounts.split(StringPool.COMMA);
        this.organizationUserService.resetGoogleKey(accounts);
        return new FebsResponse().success();
    }
}
