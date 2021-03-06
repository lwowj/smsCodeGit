package com.hero.sms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ValidateCodeService;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.common.utils.URLUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.StatisticalOrgcode;
import com.hero.sms.entity.message.StatisticalOrgcodeQuery;
import com.hero.sms.entity.organization.OrganizationUserLoginLog;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.blackIpConfig.LImitProjectEnums;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.service.message.IStatisticalOrgcodeService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserLoginLogService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.hero.sms.utils.GetSystemBrowserInfo;
import com.hero.sms.utils.StringUtil;
import com.wx.pwd.CheckStrength;
import com.wx.pwd.CheckStrength.LEVEL;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;


/**
 * @author Administrator
 */
@Validated
@RestController
@Slf4j
public class LoginController extends BaseController {

    @Autowired
    private IOrganizationUserService organizationUserService;
    @Autowired
    private ValidateCodeService validateCodeService;
    @Autowired
    private IStatisticalOrgcodeService statisticalOrgcodeService;
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IOrganizationUserLoginLogService organizationUserLoginLogService;

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
    @Limit(key = "login", period = 60, count = 5, name = "????????????", prefix = "limit")
    public FebsResponse login(
            @NotBlank(message = "{required}") String userAccount,
            @NotBlank(message = "{required}") String password,
            @NotBlank(message = "{required}") String verifyCode,
            String goologoVerifyCode,
            String sessionToken,
            String behaviorCheckToken,
            String verifyInfoToken,
            boolean rememberMe, HttpServletRequest request) {
    	
    	long start = System.currentTimeMillis();
    	String thisLoginIp = IPUtil.getIpAddr(request);
    	try 
        {
            HttpSession session = request.getSession();
            /*
             * @begin 2020-08-28
             * ???????????????????????????
             */
            if(userAccount!=null&&!"".equals(userAccount))
            {
            	 userAccount = userAccount.trim();
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
        	session.setAttribute("pwStrong", String.valueOf(passwordStrong));
        	  /**
             * @begin 2020-12-12
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
    	    	boolean checkBlackIpFlag = DatabaseCache.checkBlackIpConfigAndAllFlag(LImitProjectEnums.Organization.getCode(),thisLoginIp);
        		if(checkBlackIpFlag)
        		{
        			return new FebsResponse().message("??????IP????????????????????????????????????????????????????????????????????????").fail();
        		}
    	    }
    	    /**
    	     * @end
    	     */
            validateCodeService.check(session.getId(), verifyCode);
            organizationService.checkDomainName(URLUtil.getDomainName(request.getRequestURL().toString()),userAccount);
            organizationUserService.checkGoogleKey(userAccount,goologoVerifyCode);
            password = MD5Util.encrypt(userAccount, password);
            UsernamePasswordToken token = new UsernamePasswordToken(userAccount, password, rememberMe);
            super.login(token);
	        try 
			{
	            String thisLoginTime = DateUtil.getString(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN);
	            session.setAttribute("thisLoginIp", thisLoginIp);
	            session.setAttribute("thisLoginTime", thisLoginTime);
	            // ??????????????????
	            organizationUserService.updateLoginTime(userAccount);
			} 
			catch (Exception e) {}
	        /**
        	 * @begin 2020-12-25
        	 * ????????????????????????
        	 * ?????????????????????????????????
        	 */
	        try 
	        {
	        	OrganizationUserLoginLog organizationUserLoginLog = new OrganizationUserLoginLog();
	        	organizationUserLoginLog.setUserAccount(userAccount);//????????????
	        	organizationUserLoginLog.setLocalIp(thisLoginIp);//??????IP
	        	organizationUserLoginLog.setLoginState(CommonStateEnums.SUCCESS.getCode());//????????????
	        	organizationUserLoginLog.setMessage("????????????");//????????????
	        	OrganizationUserExt user = super.getCurrentUser();
	        	if (user != null && !StringUtils.isBlank(user.getOrganizationCode()))
	        	{
	        		organizationUserLoginLog.setOrganizationCode(user.getOrganizationCode());//????????????
	            }
	        	organizationUserLoginLogService.saveLog(start, organizationUserLoginLog);
			} catch (Exception e) {
				log.error(String.format("??????[%s]??????????????????????????????????????????", userAccount));
				e.printStackTrace();
			}
	        /**
	         * @end
	         */
        }
        catch (Exception e) 
        {
        	log.error(String.format("??????[%s]????????????:%s", userAccount,e.getMessage()));
        	/**
        	 * @begin 2020-12-25
        	 * ????????????????????????
        	 * ?????????????????????????????????
        	 */
	        try 
	        {
	        	OrganizationUserLoginLog organizationUserLoginLog = new OrganizationUserLoginLog();
	        	organizationUserLoginLog.setUserAccount(userAccount);//????????????
	        	organizationUserLoginLog.setLocalIp(thisLoginIp);//??????IP
	        	organizationUserLoginLog.setLoginState(CommonStateEnums.FAIL.getCode());//????????????
	        	String message = e.getMessage();
	        	if(StringUtil.isNotBlank(message)&&message.length()>1000)
	        	{
	        		message = message.substring(0, 1000);
	        	}
	        	organizationUserLoginLog.setMessage(message);//??????????????????
	        	organizationUserLoginLogService.saveLog(start, organizationUserLoginLog);
			} catch (Exception e1) {
				log.error(String.format("??????[%s]??????????????????????????????????????????", userAccount));
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

    @GetMapping("index/statistics")
    public FebsResponse index(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        // ??????????????????
//        this.organizationUserService.updateLoginTime(this.getCurrentUser().getUserAccount());
        //??????????????????
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode())){
            return new FebsResponse().message("?????????????????????????????????");
        }
        statisticalOrgcode.setOrgCode(user.getOrganizationCode());

        IPage<StatisticalOrgcode> datas = this.statisticalOrgcodeService.findStatisticalOrgcodes(request, statisticalOrgcode);
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        datas.getRecords().stream().forEach(item -> {
            Map<String,Object> map = new HashMap<>();
            Long reqSuccessCount = item.getReqSuccessCount()+item.getReceiptSuccessCount()+item.getReceiptFailCount();
            map.put("reqSuccessCount",reqSuccessCount);
            map.put("reqFailCount",item.getReqFailCount());
            map.put("statisticalDate",item.getStatisticalDate());
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("images/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException, FebsException {
        validateCodeService.create(request, response);
    }


}
