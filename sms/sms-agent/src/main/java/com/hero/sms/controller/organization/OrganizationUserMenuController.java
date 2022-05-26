package com.hero.sms.controller.organization;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.service.organization.IOrganizationUserMenuService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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

}
