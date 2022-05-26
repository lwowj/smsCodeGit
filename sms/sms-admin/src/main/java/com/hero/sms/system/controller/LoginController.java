package com.hero.sms.system.controller;

import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ValidateCodeService;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.monitor.entity.LoginLog;
import com.hero.sms.monitor.service.ILoginLogService;
import com.hero.sms.system.entity.User;
import com.hero.sms.system.service.IUserService;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Validated
@RestController
public class LoginController extends BaseController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ValidateCodeService validateCodeService;
    @Autowired
    private ILoginLogService loginLogService;

    @PostMapping("login")
    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public FebsResponse login(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password,
            @NotBlank(message = "{required}") String verifyCode,
            boolean rememberMe, HttpServletRequest request) throws FebsException {
        try {
        	 /*
             * @begin 2020-12-11
             * 去除字符串前后空格
             */
            if(username!=null&&!"".equals(username))
            {
            	username = username.trim();
            }
            if(password!=null&&!"".equals(password))
            {
            	password = password.trim();
            }
            if(verifyCode!=null&&!"".equals(verifyCode))
            {
            	verifyCode = verifyCode.trim();
            }
            /*
             * @end
             */
            HttpSession session = request.getSession();
            //validateCodeService.check(session.getId(), verifyCode);
            userService.checkGoogleKey(username, verifyCode);

            password = MD5Util.encrypt(username.toLowerCase(), password);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
            super.login(token);
            // 保存登录日志
            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(username);
            loginLog.setSystemBrowserInfo();
            this.loginLogService.saveLoginLog(loginLog);
        } catch (Exception e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @GetMapping("index/{username}")
    public FebsResponse index(@NotBlank(message = "{required}") @PathVariable String username) {
        // 更新登录时间
        this.userService.updateLoginTime(username);
        Map<String, Object> data = new HashMap<>();
        // 获取系统访问记录
        Long totalVisitCount = this.loginLogService.findTotalVisitCount();
        data.put("totalVisitCount", totalVisitCount);
        Long todayVisitCount = this.loginLogService.findTodayVisitCount();
        data.put("todayVisitCount", todayVisitCount);
        Long todayIp = this.loginLogService.findTodayIp();
        data.put("todayIp", todayIp);
        // 获取近期系统访问记录
        List<Map<String, Object>> lastSevenVisitCount = this.loginLogService.findLastSevenDaysVisitCount(null);
        data.put("lastSevenVisitCount", lastSevenVisitCount);
        User param = new User();
        param.setUsername(username);
        List<Map<String, Object>> lastSevenUserVisitCount = this.loginLogService.findLastSevenDaysVisitCount(param);
        data.put("lastSevenUserVisitCount", lastSevenUserVisitCount);
        return new FebsResponse().success().data(data);
    }

    @GetMapping("images/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException, FebsException {
        validateCodeService.create(request, response);
    }
}
