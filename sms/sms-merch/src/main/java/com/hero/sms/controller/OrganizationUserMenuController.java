package com.hero.sms.controller;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.service.organization.IOrganizationUserMenuService;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户用户菜单表 Controller
 *
 * @author Administrator
 * @date 2020-03-08 00:13:15
 */
@Slf4j
@Validated
@RestController
@RequestMapping("organizationUserMenu")
public class OrganizationUserMenuController extends BaseController {

    @Autowired
    private IOrganizationUserMenuService organizationUserMenuService;


    @GetMapping("tree")
    @ControllerEndpoint(exceptionMessage = "获取商户用户菜单树失败")
    @ResponseBody
    public FebsResponse getMenuTree(OrganizationUserMenu menu) {
    	/**
    	 * 2020-08-26
    	 * 设置授权标志
    	 * 0 表示商户可授权
    	 * 1 表示商户不可授权
    	 */
    	menu.setAuth("0");
    	
        MenuTree<OrganizationUserMenu> menus = this.organizationUserMenuService.findMenus(menu);
        return new FebsResponse().success().data(menus.getChilds());
    }

    @GetMapping("{userAccount}")
    public FebsResponse getOrgUserMenus(@NotBlank(message = "{required}") @PathVariable String userAccount) throws FebsException {
        OrganizationUser currentUser = getCurrentUser();
        if (!StringUtils.equalsIgnoreCase(userAccount, currentUser.getUserAccount()))
            return new FebsResponse().message("您无权获取别人的菜单").fail();
        MenuTree<OrganizationUserMenu> userMenus = this.organizationUserMenuService.findOrgUserMenusByAccount(userAccount);
        return new FebsResponse().data(userMenus);
    }
}
