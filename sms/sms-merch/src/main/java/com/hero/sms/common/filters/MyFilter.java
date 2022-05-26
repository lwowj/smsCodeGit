package com.hero.sms.common.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.blackIpConfig.LImitProjectEnums;
import com.hero.sms.service.common.IBlackIpConfigService;
import com.hero.sms.service.organization.IOrganizationUserService;

public class MyFilter extends AccessControlFilter {
    
    @Autowired
    private IOrganizationUserService organizationUserService;
    
    @Autowired
    private IBlackIpConfigService blackIpConfigService;
    
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        try {
            Subject subject = getSubject(servletRequest, servletResponse);
            String url = req.getRequestURI();
            if(subject != null && subject.isAuthenticated()){
                OrganizationUserExt user = (OrganizationUserExt) subject.getPrincipal();
                if(user != null )
                {
                	if(StringUtils.isEmpty(user.getGoogleKey())&& user.getNeedBindGoogleKey()==1  && !"/febs/views/organizationUser/compelBindGoogle".equals(url)
                            && !"/favicon.ico".equals(url) && !"/".equals(url) && !"/login".equals(url)&& !"/organizationUser/bindGoogle".equals(url))
                    {
                        WebUtils.redirectToSavedRequest(servletRequest, servletResponse, "/febs/views/organizationUser/compelBindGoogle");
                        return false;
                    }
                	
                	String thisIP = IPUtil.getIpAddr(req);
                	/**
                	 * @begin 2020-12-21
                	 * 判断登录IP与操作IP是否一致，若不一致，强制下线
                	 */
                	String checkMerchLoginIpSwitch = "ON";
            		Code checkMerchLoginIpSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkMerchLoginIpSwitch");
            	    if(checkMerchLoginIpSwitchCode!=null&&!"".equals(checkMerchLoginIpSwitchCode.getName()))
            	    {
            	    	checkMerchLoginIpSwitch = checkMerchLoginIpSwitchCode.getName();
            	    }
            	    if(!"OFF".equals(checkMerchLoginIpSwitch))
            	    {
            	    	//校验IP与登录时的IP不一致时，跳回登录界面
                    	if(!"/favicon.ico".equals(url) && !"/".equals(url) && !"/login".equals(url))
                        {
                    		String userAccount = user.getUserAccount();
                    		// 获取spring bean
                    		if(organizationUserService==null) 
                    		{
                    			organizationUserService = SpringContextUtil.getBean(IOrganizationUserService.class);
                    		}
                    		OrganizationUser orgUser = organizationUserService.findByUserAccount(userAccount);
                    		String loginIP = orgUser.getLoginIp();
                    		if(!loginIP.equals(thisIP))
                    		{
                    			WebUtils.redirectToSavedRequest(servletRequest, servletResponse, "/logout");
                                return false;
                    		}
                        }
            	    }
            	    /**
            	     * @end
            	     */
            	    /**
                	 * @begin 2020-12-21
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
                    	if(!"/favicon.ico".equals(url) && !"/".equals(url) && !"/login".equals(url))
                        {
                    		boolean checkBlackIpFlag = DatabaseCache.checkBlackIpConfigAndAllFlag(LImitProjectEnums.Organization.getCode(),thisIP);
                    		if(checkBlackIpFlag)
                    		{
                    			WebUtils.redirectToSavedRequest(servletRequest, servletResponse, "/logout");
                    			return false;
                    		}
                        }
            	    }
            	    /**
            	     * @end
            	     */
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            redirectToLogin(servletRequest,servletResponse);
            return false;
        }
        return true;
    }
}
