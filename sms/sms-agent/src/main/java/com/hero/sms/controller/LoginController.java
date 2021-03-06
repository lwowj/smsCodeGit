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
    @Limit(key = "loginBehavior", period = 10, count = 5, name = "??????????????????", prefix = "limit")
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
    
    @ControllerEndpoint(operation = "??????" , exceptionMessage = "????????????")
    @PostMapping("login")
    @Limit(key = "login", period = 60, count = 20, name = "????????????", prefix = "limit")
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
             * ???????????????????????????
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
          //??????token????????????
            if(StringUtil.isBlank(sessionToken))
            {
            	return new FebsResponse().message("??????????????????,????????????????????????").fail();
            }
          //????????????token????????????
            if(StringUtil.isBlank(behaviorCheckToken))
            {
            	return new FebsResponse().message("????????????????????????????????????").fail();
            }
          //?????????????????????????????????token????????????
            if(StringUtil.isBlank(verifyInfoToken))
            {
            	return new FebsResponse().message("????????????????????????????????????").fail();
            }
          //?????????????????????????????????token?????????????????????????????????
            String checkInfoToken = GetSystemBrowserInfo.getSystemBrowserInfoToken(request);
    		if(!verifyInfoToken.equals(checkInfoToken))
    		{
    			return new FebsResponse().message("??????????????????,??????????????????").fail();
    		}
            /*
             * @end
             */
            LEVEL passwordStrong = LEVEL.MIDIUM;
        	try 
        	{
        		/**
	        	  *  ????????????????????????
	        	 * EASY				??????
	        	 * MIDIUM			??????
	        	 * STRONG			???
	        	 * VERY_STRONG		??????
	        	 * EXTREMELY_STRONG	??????
	        	 */
        		passwordStrong = new CheckStrength().getPasswordLevel(password);
			} catch (Exception e) {}
        	session.setAttribute("agentPwStrong", String.valueOf(passwordStrong));
        	
        	 /**
             * @begin 2020-12-13
             * ????????????token?????????????????????
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
    	        	return new FebsResponse().message("??????????????????,????????????????????????").fail();
    			}
    	        if (!token.isTokenValid(request,true,AjaxTokenProcessor.LOGIN_BEHAVIORCHECK_TOKEN_KEY, behaviorCheckToken)) 
    			{
    	        	return new FebsResponse().message("??????????????????,??????????????????????????????").fail();
    			}
    	    }
            /**
             * @end
             */
    	    /**
        	 * @begin 2020-12-23
        	 * ???????????????IP
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
        			return new FebsResponse().message("??????IP????????????????????????????????????????????????????????????????????????").fail();
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
            	// ??????????????????
                this.agentService.updateLoginTime(username, IPUtil.getIpAddr(request));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
            
            /**
        	 * @begin 2020-12-25
        	 * ????????????????????????
        	 * ?????????????????????????????????
        	 */
	        try 
	        {
	        	AgentLoginLog agentLoginLog = new AgentLoginLog();
	        	agentLoginLog.setAgentAccount(username);//????????????
	        	agentLoginLog.setLocalIp(thisLoginIp);//??????IP
	        	agentLoginLog.setLoginState(CommonStateEnums.SUCCESS.getCode());//????????????
	        	agentLoginLog.setMessage("????????????");//????????????
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
        	 * ????????????????????????
        	 * ?????????????????????????????????
        	 */
	        try 
	        {
	        	AgentLoginLog agentLoginLog = new AgentLoginLog();
	        	agentLoginLog.setAgentAccount(username);//????????????
	        	agentLoginLog.setLocalIp(thisLoginIp);//??????IP
	        	agentLoginLog.setLoginState(CommonStateEnums.FAIL.getCode());//????????????
	        	String message = e.getMessage();
	        	if(StringUtil.isNotBlank(message)&&message.length()>1000)
	        	{
	        		message = message.substring(0, 1000);
	        	}
	        	agentLoginLog.setMessage(message);//??????????????????
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
//        // ??????????????????
//        this.agentService.updateLoginTime(username, IPUtil.getIpAddr(request));
        Map<String, Object> data = new HashMap<>();

        return new FebsResponse().success().data(data);
    }

    /**
     * ????????????
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
