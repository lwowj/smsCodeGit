package com.hero.sms.controller.agent;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenu;
import com.hero.sms.service.agent.IAgentMenuService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @GetMapping("agentMenu/{username}")
    @ResponseBody
    public FebsResponse getUserMenus(@NotBlank(message = "{required}") @PathVariable String username) throws FebsException {
        Agent currentUser = getCurrentAgent();
        if (!StringUtils.equalsIgnoreCase(username, currentUser.getAgentAccount()))
            return new FebsResponse().message("您无权获取别人的菜单").fail();
        MenuTree<AgentMenu> userMenus = this.agentMenuService.findMenus(username,getCurrentAgent());
        return new FebsResponse().data(userMenus);
    }


    @GetMapping("agentMenu/tree")
    @ControllerEndpoint(exceptionMessage = "获取代理菜单树失败")
    @ResponseBody
    public FebsResponse getMenuTree(AgentMenu menu) {
        MenuTree<AgentMenu> menus = this.agentMenuService.findMenus(menu);
        return new FebsResponse().success().data(menus.getChilds());
    }
}
