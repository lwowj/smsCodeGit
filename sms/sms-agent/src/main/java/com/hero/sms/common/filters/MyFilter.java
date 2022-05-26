package com.hero.sms.common.filters;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.common.BlackIpConfig;
import com.hero.sms.entity.common.BlackIpConfigQuery;
import com.hero.sms.entity.common.Code;
import com.hero.sms.enums.blackIpConfig.IsAvailabilityEnums;
import com.hero.sms.enums.blackIpConfig.LImitProjectEnums;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.common.IBlackIpConfigService;

public class MyFilter extends AccessControlFilter {
    @Autowired
    private IAgentService agentService;
    
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        try {
            Subject subject = getSubject(servletRequest, servletResponse);
            if(subject != null && subject.isAuthenticated()){
                Agent user = (Agent) subject.getPrincipal();
                String url = req.getRequestURI();
                if(user != null)
                {
                	 if(StringUtils.isEmpty(user.getGoogleKey()) && user.getNeedBindGoogleKey()==1 && !"/febs/views/agent/compelBindGoogle".equals(url)
                             && !"/favicon.ico".equals(url) && !"/".equals(url) && !"/login".equals(url)&& !"/agent/bindGoogle".equals(url)){
                         WebUtils.redirectToSavedRequest(servletRequest, servletResponse, "/febs/views/agent/compelBindGoogle");
                         return false;
                     }
                     
                	 String thisIP = IPUtil.getIpAddr(req);
                 	/**
                 	 * @begin 2020-12-21
                 	 * 判断登录IP与操作IP是否一致，若不一致，强制下线
                 	 */
                    String checkAgentLoginIpSwitch = "ON";
             		Code checkAgentLoginIpSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkAgentLoginIpSwitch");
             	    if(checkAgentLoginIpSwitchCode!=null&&!"".equals(checkAgentLoginIpSwitchCode.getName()))
             	    {
             	    	checkAgentLoginIpSwitch = checkAgentLoginIpSwitchCode.getName();
             	    }
             	    if(!"OFF".equals(checkAgentLoginIpSwitch))
             	    {
             	    	//校验IP与登录时的IP不一致时，跳回登录界面
                    	if(!"/favicon.ico".equals(url) && !"/".equals(url) && !"/login".equals(url))
                        {
                    		String agentAccount = user.getAgentAccount();
                    		// 获取spring bean
                    		if(agentService==null) 
                    		{
                    			agentService = SpringContextUtil.getBean(IAgentService.class);
                    		}
                    		Agent currentUserDetail = agentService.findByAccount(agentAccount);
                    		String loginIP = currentUserDetail.getLoginIp();
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
                    		boolean checkBlackIpFlag = DatabaseCache.checkBlackIpConfigAndAllFlag(LImitProjectEnums.Agent.getCode(),thisIP);
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
