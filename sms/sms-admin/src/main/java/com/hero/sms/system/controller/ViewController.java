package com.hero.sms.system.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.ExpiredSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hero.sms.common.authentication.ShiroHelper;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.common.CodeSort;
import com.hero.sms.service.common.ICodeService;
import com.hero.sms.service.common.ICodeSortService;
import com.hero.sms.system.entity.User;
import com.hero.sms.system.service.IUserService;

/**
 * @author Administrator
 */
@Controller("systemView")
public class ViewController extends BaseController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ICodeSortService codeSortService;
    @Autowired
    private ICodeService codeService;
    @Autowired
    private ShiroHelper shiroHelper;

    @GetMapping("login")
    @ResponseBody
    public Object login(HttpServletRequest request) {
        if (AppUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName(FebsUtil.view("login"));
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
    public String index(Model model) {
        AuthorizationInfo authorizationInfo = shiroHelper.getCurrentuserAuthorizationInfo();
        User user = super.getCurrentUser();
        User currentUserDetail = userService.findByName(user.getUsername());
        currentUserDetail.setPassword("It's a secret");
        model.addAttribute("user", currentUserDetail);
        model.addAttribute("permissions", authorizationInfo.getStringPermissions());
        model.addAttribute("roles", authorizationInfo.getRoles());
        return "index";
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "layout")
    public String layout() {
        return FebsUtil.view("layout");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "password/update")
    public String passwordUpdate() {
        return FebsUtil.view("system/user/passwordUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/profile")
    public String userProfile() {
        return FebsUtil.view("system/user/userProfile");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/avatar")
    public String userAvatar() {
        return FebsUtil.view("system/user/avatar");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/profile/update")
    public String profileUpdate() {
        return FebsUtil.view("system/user/profileUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user")
    @RequiresPermissions("user:view")
    public String systemUser() {
        return FebsUtil.view("system/user/user");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/add")
    @RequiresPermissions("user:add")
    public String systemUserAdd() {
        return FebsUtil.view("system/user/userAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/detail/{username}")
    @RequiresPermissions("user:view")
    public String systemUserDetail(@PathVariable String username, Model model) {
        resolveUserModel(username, model, true);
        return FebsUtil.view("system/user/userDetail");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/update/{username}")
    @RequiresPermissions("user:update")
    public String systemUserUpdate(@PathVariable String username, Model model) {
        resolveUserModel(username, model, false);
        return FebsUtil.view("system/user/userUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/googleKey/{username}")
    @RequiresPermissions("user:google:key")
    public String getUserGoogleKey(@PathVariable String username, Model model) {
        User user = userService.findByName(username);
        String googleQrcode = GoogleAuthenticator.getQRBarcode(user.getUsername(), user.getGoogleKey());
        model.addAttribute("googleQrcode", googleQrcode);
        return FebsUtil.view("system/user/googleKey");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/role")
    @RequiresPermissions("role:view")
    public String systemRole() {
        return FebsUtil.view("system/role/role");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/menu")
    @RequiresPermissions("menu:view")
    public String systemMenu() {
        return FebsUtil.view("system/menu/menu");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/dept")
    @RequiresPermissions("dept:view")
    public String systemDept() {
        return FebsUtil.view("system/dept/dept");
    }

    //字典 - 分类
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/codeSort")
    @RequiresPermissions("codeSort:view")
    public String codeSort(){
        return FebsUtil.view("system/codeSort/codeSort");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/codeSort/add")
    @RequiresPermissions("codeSort:add")
    public String systemcodeSortAdd() {
        return FebsUtil.view("system/codeSort/codeSortAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/codeSort/update/{id}")
    @RequiresPermissions("codeSort:update")
    public String codeSortUpdate(@PathVariable String id, Model model) {
        resolveCodeSortModel(id, model);
        return FebsUtil.view("system/codeSort/codeSortUpdate");
    }


    //字典
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/code")
    @RequiresPermissions("code:view")
    public String code(){
        return FebsUtil.view("system/code/code");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/code/add")
    @RequiresPermissions("code:add")
    public String systemCodeAdd() {
        return FebsUtil.view("system/code/codeAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/code/update/{id}")
    @RequiresPermissions("code:update")
    public String codeUpdate(@PathVariable String id, Model model) {
        resolveCodeModel(id, model);
        return FebsUtil.view("system/code/codeUpdate");
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

    private void resolveUserModel(String username, Model model, Boolean transform) {
        User user = userService.findByName(username);
        model.addAttribute("user", user);
        if (transform) {
            String ssex = user.getSex();
            if (User.SEX_MALE.equals(ssex)) user.setSex("男");
            else if (User.SEX_FEMALE.equals(ssex)) user.setSex("女");
            else user.setSex("保密");
        }
        if (user.getLastLoginTime() != null)
            model.addAttribute("lastLoginTime", DateUtil.getDateFormat(user.getLastLoginTime(), DateUtil.FULL_TIME_SPLIT_PATTERN));
    }

    private void resolveCodeSortModel(String id, Model model) {
        CodeSort codeSort = this.codeSortService.findById(id);
        model.addAttribute("codeSort",codeSort);
    }

    private void resolveCodeModel(String id, Model model) {
        Code code = this.codeService.getById(id);
        model.addAttribute("code",code);
    }
}
