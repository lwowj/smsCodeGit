package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.AgentMenu;
import com.hero.sms.service.agent.IAgentMenuService;
import com.hero.sms.system.entity.Menu;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 代理菜单表 Controller
 *
 * @author Administrator
 * @date 2020-03-06 10:05:44
 */
@Slf4j
@Validated
@Controller
public class AgentMenuController extends BaseController {

    @Autowired
    private IAgentMenuService agentMenuService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentMenu")
    public String agentMenuIndex(){
        return FebsUtil.view("agentMenu/agentMenu");
    }

    @ControllerEndpoint(operation = "代理菜单列表")
    @GetMapping("agentMenu")
    @ResponseBody
    @RequiresPermissions("agentMenu:list")
    public FebsResponse getAllAgentMenus(AgentMenu agentMenu) {
        return new FebsResponse().success().data(agentMenuService.findAgentMenus(agentMenu));
    }

    @ControllerEndpoint(operation = "代理菜单列表")
    @GetMapping("agentMenu/list")
    @ResponseBody
    @RequiresPermissions("agentMenu:list")
    public FebsResponse agentMenuList(QueryRequest request, AgentMenu agentMenu) {
        Map<String, Object> dataTable = getDataTable(this.agentMenuService.findAgentMenus(request, agentMenu));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "代理菜单新增")
    @PostMapping("agentMenu")
    @ResponseBody
    @RequiresPermissions("agentMenu:add")
    public FebsResponse addAgentMenu(@Valid AgentMenu agentMenu) {
        this.agentMenuService.createAgentMenu(agentMenu);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理菜单删除")
    @GetMapping("agentMenu/delete")
    @ResponseBody
    @RequiresPermissions("agentMenu:delete")
    public FebsResponse deleteAgentMenu(AgentMenu agentMenu) {
        this.agentMenuService.deleteAgentMenu(agentMenu);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理菜单更新")
    @PostMapping("agentMenu/update")
    @ResponseBody
    @RequiresPermissions("agentMenu:update")
    public FebsResponse updateAgentMenu(AgentMenu agentMenu) {
        this.agentMenuService.updateAgentMenu(agentMenu);
        return new FebsResponse().success();
    }

    @PostMapping("agentMenu/excel")
    @ResponseBody
    @RequiresPermissions("agentMenu:export")
    public void export(QueryRequest queryRequest, AgentMenu agentMenu, HttpServletResponse response) {
        List<AgentMenu> agentMenus = this.agentMenuService.findAgentMenus(queryRequest, agentMenu).getRecords();
        ExcelKit.$Export(AgentMenu.class, response).downXlsx(agentMenus, false);
    }

    @GetMapping("agentMenu/tree")
    @ControllerEndpoint(exceptionMessage = "获取代理菜单树失败")
    @ResponseBody
    public FebsResponse getMenuTree(AgentMenu menu) {
        MenuTree<AgentMenu> menus = this.agentMenuService.findMenus(menu);
        return new FebsResponse().success().data(menus.getChilds());
    }

    @ControllerEndpoint(operation = "代理菜单删除")
    @GetMapping("agentMenu/delete/{menuIds}")
    @RequiresPermissions("agentMenu:delete")
    @ResponseBody
    public FebsResponse deleteMenus(@NotBlank(message = "{required}") @PathVariable String menuIds) {
        this.agentMenuService.deleteMeuns(menuIds);
        return new FebsResponse().success();
    }
}
