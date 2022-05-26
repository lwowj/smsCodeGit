package com.hero.sms.controller.organization;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ISessionService;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.service.organization.IOrganizationUserService;

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

    @GetMapping("check/{userAccount}")
    public boolean checkUserAccount(@NotBlank(message = "{required}") @PathVariable String userAccount, String userId){
        OrganizationUser orgUser = this.organizationUserService.findByUserAccount(userAccount);
        return orgUser==null || StringUtils.isNotBlank(userId);
    }

    @ControllerEndpoint(operation = "商户用户列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationUser:view")
    public FebsResponse organizationUserList(QueryRequest request, OrganizationUser organizationUser) {
        Agent agent = getCurrentAgent();
        Map<String, Object> dataTable = getDataTable(this.organizationUserService.selectLeftOrganizationPage(request, organizationUser, agent));
        dataTable.remove("userPassword");
        dataTable.remove("googleKey");
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增商户用户")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationUser:add")
    public FebsResponse addOrganizationUser(HttpServletRequest request, @Valid OrganizationUser organizationUser) {
        String userPasswordConFirm = request.getParameter("userPasswordConFirm");
        if(!userPasswordConFirm.equals(organizationUser.getUserPassword())){
            return new FebsResponse().message("两次输入的密码不一致！").fail();
        }
        try {
            organizationUser.setAccountType(DatabaseCache.getCodeBySortCodeAndName("AccountType","子账户").getCode());
            organizationUser.setCreateUser(getCurrentAgent().getAgentAccount());
            organizationUser.setCreateDate(new Date());
            if(organizationUser.getNeedBindGoogleKey()==null)
            {
            	int needBindGoogleKey = 0;
            	String orgDefaultNeedBindgoogleKey = "0";
        		Code orgDefaultNeedBindgoogleKeyCode = DatabaseCache.getCodeBySortCodeAndCode("System","orgDefaultNeedBindgoogleKey");
        	    if(orgDefaultNeedBindgoogleKeyCode!=null&&!"".equals(orgDefaultNeedBindgoogleKeyCode.getName()))
        	    {
        	    	orgDefaultNeedBindgoogleKey = orgDefaultNeedBindgoogleKeyCode.getName();
        	    }
        	    if("1".equals(orgDefaultNeedBindgoogleKey))
        	    {
        	    	needBindGoogleKey = 1;
        	    }
        	    organizationUser.setNeedBindGoogleKey(needBindGoogleKey);
            }
            this.organizationUserService.createOrganizationUser(organizationUser,getCurrentAgent());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("新增商户用户失败",e);
            return new FebsResponse().message("新增商户用户失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "更新商户用户")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationUser:update")
    public FebsResponse updateOrganizationUser(OrganizationUser organizationUser) {
        try {
            this.organizationUserService.updateOrganizationUser(organizationUser,getCurrentAgent());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("更新商户用户失败",e);
            return new FebsResponse().message("更新商户用户失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "解锁商户用户")
    @GetMapping("unlock/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:unlock")
    public FebsResponse unlockOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        Agent agent = getCurrentAgent();
        this.organizationUserService.updateUserStatus(ids,"1",agent);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "锁定商户用户")
    @GetMapping("lock/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:lock")
    public FebsResponse lockOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        Agent agent = getCurrentAgent();
        this.organizationUserService.updateUserStatus(ids,"0",agent);
        //踢出用户
        this.sessionService.forceLogoutOrgUser(organizationUserIds,agent.getId());
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
}
