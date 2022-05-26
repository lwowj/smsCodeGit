package com.hero.sms.controller;

import com.hero.sms.common.authentication.ShiroHelper;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.utils.AjaxTokenProcessor;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.session.ExpiredSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Administrator
 */
@Controller("agentLoginView")
public class ViewController extends BaseController {

    @Autowired
    private IAgentService agentService;
    @Autowired
    private ShiroHelper shiroHelper;

    @GetMapping("login")
    @ResponseBody
    public Object login(HttpServletRequest request) {
        if (AppUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        } else {
            ModelAndView mav = new ModelAndView();
            /**
             * @begin 2020-12-13
             * 新增初始token 防止重复提交
             */
            String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.LOGIN_TRANSACTION_TOKEN_KEY);
            mav.addObject("sessionToken", sessionToken);
            /**
             * @end
             */
            mav.setViewName(FebsUtil.view("loginB"));
            return mav;
        }
    }

    @GetMapping("unauthorized")
    public String unauthorized() {
        return FebsUtil.view("error/403");
    }


    @GetMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }

    @GetMapping("index")
    public String index(Model model,HttpServletRequest request) {
        AuthorizationInfo authorizationInfo = shiroHelper.getCurrentuserAuthorizationInfo();
        Agent agent = super.getCurrentAgent();
        Agent currentUserDetail = agentService.findByAccount(agent.getAgentAccount());
        currentUserDetail.setGoogleKey(null);
        currentUserDetail.setPayPassword(null);
        currentUserDetail.setDataMd5(null);
        currentUserDetail.setCreateUser(null);
        currentUserDetail.setAgentPassword("It's a secret");
        model.addAttribute("user", currentUserDetail);
        model.addAttribute("permissions", authorizationInfo.getStringPermissions());
        model.addAttribute("roles", authorizationInfo.getRoles());
        HttpSession session = request.getSession();
        String agentPwStrong = String.valueOf(session.getAttribute("agentPwStrong"));
        model.addAttribute("agentPwStrong", agentPwStrong);
        return "index";
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "layout")
    public String layout() {
        return FebsUtil.view("layout");
    }


    @RequestMapping(FebsConstant.VIEW_PREFIX + "index")
    public String pageIndex() {
        return FebsUtil.view("index");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "404")
    public String error404() {
        return FebsUtil.view("error/404");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "403")
    public String error403() {
        return FebsUtil.view("error/403");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "500")
    public String error500() {
        return FebsUtil.view("error/500");
    }


}
