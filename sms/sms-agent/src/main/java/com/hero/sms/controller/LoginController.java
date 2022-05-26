package com.hero.sms.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ValidateCodeService;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.common.utils.URLUtil;
import com.hero.sms.entity.agent.AgentLoginLog;
import com.hero.sms.entity.common.Code;
import com.hero.sms.enums.blackIpConfig.LImitProjectEnums;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.service.agent.IAgentLoginLogService;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.hero.sms.utils.GetSystemBrowserInfo;
import com.hero.sms.utils.StringUtil;
import com.wx.pwd.CheckStrength;
import com.wx.pwd.CheckStrength.LEVEL;

/**
 * @author Administrator
 */
@Validated
@RestController
public class LoginController extends BaseController {

    @Autowired
    private IAgentService agentService;
    @Autowired
    private ValidateCodeService validateCodeService;
    @Autowired
    private IAgentLoginLogService agentLoginLogService;
    
    @PostMapping("loginBehavior")
    @Limit(key = "loginBehavior", period = 10, count = 5, name = "登录行为验证", prefix = "limit")
    public FebsResponse loginBehavior(HttpServletRequest request) 
    {
    	FebsResponse febsResponse = new FebsResponse();
    	String behaviorCheckToken = "";
    	String verifyInfoToken = "";
    	try 
        {
    		verifyInfoToken = GetSystemBrowserInfo.getSystemBrowserInfoToken(request);
    		if(StringUtil.isBlank(verifyInfoToken))
    		{
    			 StringBuffer checkStr = new StringBuffer();
				 checkStr.append("1|2|3|").append(MD5Util.defaultKey);
				 verifyInfoToken = MD5Util.MD5(checkStr.toString());
    		}
    		behaviorCheckToken = getTokenInputStr(request,AjaxTokenProcessor.LOGIN_BEHAVIORCHECK_TOKEN_KEY);
    		febsResponse.put("behaviorCheckToken", behaviorCheckToken);
    		febsResponse.put("verifyInfoToken", verifyInfoToken);
            return febsResponse.success();
        }
        catch (Exception e) 
        {
            febsResponse.message(e.getMessage());
            febsResponse.put("behaviorCheckToken", behaviorCheckToken);
            febsResponse.put("verifyInfoToken", verifyInfoToken);
            return febsResponse.fail();
        }
        
    }
    
    @ControllerEndpoint(operation = "登录" , exceptionMessage = "登录失败")
    @PostMapping("login")
    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public FebsResponse login(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password,
            @NotBlank(message = "{required}") String verifyCode,
            String goologoVerifyCode,
            String sessionToken,
            String behaviorCheckToken,
            String verifyInfoToken,
            boolean rememberMe, HttpServletRequest request){
    	
    	long start = System.currentTimeMillis();
    	String thisLoginIp = IPUtil.getIpAddr(request);
    	
        try 
        {
            HttpSession session = request.getSession();
            /*
             * @begin 2020-08-28
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
            if(goologoVerifyCode!=null&&!"".equals(goologoVerifyCode))
            {
            	goologoVerifyCode = goologoVerifyCode.trim();
            }
          //判断token是否为空
            if(StringUtil.isBlank(sessionToken))
            {
            	return new FebsResponse().message("当前请求无效,请刷新页面重试！").fail();
            }
          //判断滑块token是否为空
            if(StringUtil.isBlank(behaviorCheckToken))
            {
            	return new FebsResponse().message("请先拖动滑块，进行解锁！").fail();
            }
          //判断滑块获取的环境信息token是否为空
            if(StringUtil.isBlank(verifyInfoToken))
            {
            	return new FebsResponse().message("请先拖动滑块，进行解锁！").fail();
            }
          //判断滑块获取的环境信息token与当前环境信息是否一致
            String checkInfoToken = GetSystemBrowserInfo.getSystemBrowserInfoToken(request);
    		if(!verifyInfoToken.equals(checkInfoToken))
    		{
    			return new FebsResponse().message("解锁验证失败,请重新解锁！").fail();
    		}
            /*
             * @end
             */
            LEVEL passwordStrong = LEVEL.MIDIUM;
        	try 
        	{
        		/**
	        	  *  获取密码强度信息
	        	 * EASY				简单
	        	 * MIDIUM			中等
	        	 * STRONG			强
	        	 * VERY_STRONG		很强
	        	 * EXTREMELY_STRONG	极强
	        	 */
        		passwordStrong = new CheckStrength().getPasswordLevel(password);
			} catch (Exception e) {}
        	session.setAttribute("agentPwStrong", String.valueOf(passwordStrong));
        	
        	 /**
             * @begin 2020-12-13
             * 新增校验token，防止重复提交
             */
            String checkSessionTokenSwitch = "ON";
    		Code checkSessionTokenSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkSessionTokenSwitch");
    	    if(checkSessionTokenSwitchCode!=null&&!"".equals(checkSessionTokenSwitchCode.getName()))
    	    {
    	    	checkSessionTokenSwitch = checkSessionTokenSwitchCode.getName();
    	    }
    	    if(!"OFF".equals(checkSessionTokenSwitch))
    	    {
    	    	AjaxTokenProcessor token = AjaxTokenProcessor.getInstance();
    	        if (!token.isTokenValid(request,true,AjaxTokenProcessor.LOGIN_TRANSACTION_TOKEN_KEY, sessionToken)) 
    			{
    	        	return new FebsResponse().message("当前请求无效,请刷新页面重试！").fail();
    			}
    	        if (!token.isTokenValid(request,true,AjaxTokenProcessor.LOGIN_BEHAVIORCHECK_TOKEN_KEY, behaviorCheckToken)) 
    			{
    	        	return new FebsResponse().message("解锁验证异常,请刷新页面重新解锁！").fail();
    			}
    	    }
            /**
             * @end
             */
    	    /**
        	 * @begin 2020-12-23
        	 * 过滤黑名单IP
        	 */
        	String checkBlackIpSwitch = "ON";
    		Code checkBlackIpSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkBlackIpSwitch");
    	    if(checkBlackIpSwitchCode!=null&&!"".equals(checkBlackIpSwitchCode.getName()))
    	    {
    	    	checkBlackIpSwitch = checkBlackIpSwitchCode.getName();
    	    }
    	    if(!"OFF".equals(checkBlackIpSwitch))
    	    {
    	    	boolean checkBlackIpFlag = DatabaseCache.checkBlackIpConfigAndAllFlag(LImitProjectEnums.Agent.getCode(),thisLoginIp);
        		if(checkBlackIpFlag)
        		{
        			return new FebsResponse().message("当前IP受限，请联系客服人员处理，给你带来不便，请谅解！").fail();
        		}
    	    }
    	    /**
    	     * @end
    	     */
            validateCodeService.check(session.getId(), verifyCode);
            agentService.checkDomainName(URLUtil.getDomainName(request.getRequestURL().toString()),username);
            agentService.checkGoogleKey(username, goologoVerifyCode);
            password = MD5Util.encrypt(username, password);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
            super.login(token);
            try {
            	// 更新登录时间
                this.agentService.updateLoginTime(username, IPUtil.getIpAddr(request));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
            
            /**
        	 * @begin 2020-12-25
        	 * 新增登录日志入库
        	 * 入库异常，不影响原流程
        	 */
	        try 
	        {
	        	AgentLoginLog agentLoginLog = new AgentLoginLog();
	        	agentLoginLog.setAgentAccount(username);//登录账号
	        	agentLoginLog.setLocalIp(thisLoginIp);//登录IP
	        	agentLoginLog.setLoginState(CommonStateEnums.SUCCESS.getCode());//成功状态
	        	agentLoginLog.setMessage("登录成功");//登录信息
	        	agentLoginLogService.saveLog(start, agentLoginLog);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        /**
	         * @end
	         */
	        
        } catch (Exception e) {
            /**
        	 * @begin 2020-12-25
        	 * 新增登录日志入库
        	 * 入库异常，不影响原流程
        	 */
	        try 
	        {
	        	AgentLoginLog agentLoginLog = new AgentLoginLog();
	        	agentLoginLog.setAgentAccount(username);//登录账号
	        	agentLoginLog.setLocalIp(thisLoginIp);//登录IP
	        	agentLoginLog.setLoginState(CommonStateEnums.FAIL.getCode());//失败状态
	        	String message = e.getMessage();
	        	if(StringUtil.isNotBlank(message)&&message.length()>1000)
	        	{
	        		message = message.substring(0, 1000);
	        	}
	        	agentLoginLog.setMessage(message);//登录失败信息
	        	agentLoginLogService.saveLog(start, agentLoginLog);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	        /**
	         * @end
	         */
        	FebsResponse febsResponse = new FebsResponse();
            febsResponse.message(e.getMessage());
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.LOGIN_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        }
        return new FebsResponse().success();
    }


    @GetMapping("index/{username}")
    public FebsResponse index(@NotBlank(message = "{required}") @PathVariable String username,HttpServletRequest request) {
//        // 更新登录时间
//        this.agentService.updateLoginTime(username, IPUtil.getIpAddr(request));
        Map<String, Object> data = new HashMap<>();

        return new FebsResponse().success().data(data);
    }

    /**
     * 首页统计
     * @return
     */
    @GetMapping("sumOrgcode")
    public FebsResponse sumOrgcode(){
        String data = agentService.statisticalOnAgent(getCurrentAgent());
        return new FebsResponse().success().data(data);
    }

    @GetMapping("images/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException, FebsException {
        validateCodeService.create(request, response);
    }
}
