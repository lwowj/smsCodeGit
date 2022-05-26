package com.hero.sms.controller.organization;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.service.organization.IOrganizationUserMenuService;
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
 * 商户用户菜单表 Controller
 *
 * @author Administrator
 * @date 2020-03-08 00:13:15
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationUserMenu")
public class OrganizationUserMenuController extends BaseController {

    @Autowired
    private IOrganizationUserMenuService organizationUserMenuService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUserMenu")
    public String organizationUserMenuIndex(){
        return FebsUtil.view("organization/organizationUserMenu");
    }

    @ControllerEndpoint(operation = "商户用户菜单列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:view")
    public FebsResponse getAllOrganizationUserMenus(OrganizationUserMenu organizationUserMenu) {
        return new FebsResponse().success().data(organizationUserMenuService.findOrganizationUserMenus(organizationUserMenu));
    }

    @ControllerEndpoint(operation = "商户用户菜单列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:view")
    public FebsResponse organizationUserMenuList(QueryRequest request, OrganizationUserMenu organizationUserMenu) {
        Map<String, Object> dataTable = getDataTable(this.organizationUserMenuService.findOrganizationUserMenus(request, organizationUserMenu));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "商户用户菜单新增")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:add")
    public FebsResponse addOrganizationUserMenu(@Valid OrganizationUserMenu organizationUserMenu) {
        this.organizationUserMenuService.createOrganizationUserMenu(organizationUserMenu);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户菜单删除")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:delete")
    public FebsResponse deleteOrganizationUserMenu(OrganizationUserMenu organizationUserMenu) {
        this.organizationUserMenuService.deleteOrganizationUserMenu(organizationUserMenu);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户菜单删除")
    @GetMapping("delete/{organizationUserMenuIds}")
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:delete")
    public FebsResponse deleteOrganizationUserMenu(@NotBlank(message = "{required}") @PathVariable String organizationUserMenuIds) {
        String[] ids = organizationUserMenuIds.split(StringPool.COMMA);
        this.organizationUserMenuService.deleteOrganizationUserMenus(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户用户菜单更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:update")
    public FebsResponse updateOrganizationUserMenu(OrganizationUserMenu organizationUserMenu) {
        this.organizationUserMenuService.updateOrganizationUserMenu(organizationUserMenu);
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationUserMenu:export")
    public void export(QueryRequest queryRequest, OrganizationUserMenu organizationUserMenu, HttpServletResponse response) {
        List<OrganizationUserMenu> organizationUserMenus = this.organizationUserMenuService.findOrganizationUserMenus(queryRequest, organizationUserMenu).getRecords();
        ExcelKit.$Export(OrganizationUserMenu.class, response).downXlsx(organizationUserMenus, false);
    }

    @GetMapping("tree")
    @ControllerEndpoint(exceptionMessage = "获取商户用户菜单树失败")
    @ResponseBody
    public FebsResponse getMenuTree(OrganizationUserMenu menu) {
        MenuTree<OrganizationUserMenu> menus = this.organizationUserMenuService.findMenus(menu);
        return new FebsResponse().success().data(menus.getChilds());
    }

    @GetMapping("organizationUserMenu/delete/{menuIds}")
    @RequiresPermissions("organizationUserMenu:delete")
    @ControllerEndpoint(operation = "删除菜单/按钮", exceptionMessage = "删除菜单/按钮失败")
    @ResponseBody
    public FebsResponse deleteMenus(@NotBlank(message = "{required}") @PathVariable String menuIds) {
        this.organizationUserMenuService.deleteMeuns(menuIds);
        return new FebsResponse().success();
    }
}
