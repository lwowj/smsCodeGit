package com.hero.sms.common.controller;

import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.system.entity.User;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 */
public class BaseController {

    private Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    protected User getCurrentUser() {
        return (User) getSubject().getPrincipal();
    }

    protected Session getSession() {
        return getSubject().getSession();
    }

    protected Session getSession(Boolean flag) {
        return getSubject().getSession(flag);
    }

    protected void login(AuthenticationToken token) {
        getSubject().login(token);
    }

    protected Map<String, Object> getDataTable(IPage<?> pageInfo) {
        return getDataTable(pageInfo, FebsConstant.DATA_MAP_INITIAL_CAPACITY);
    }

    protected Map<String, Object> getDataTable(Long total, List records){
        Map<String, Object> data = new HashMap<>(4);
        data.put("rows", records);
        data.put("total", total);
        return data;
    }

    protected Map<String, Object> getDataTable(IPage<?> pageInfo, int dataMapInitialCapacity) {
        Map<String, Object> data = new HashMap<>(dataMapInitialCapacity);
        data.put("rows", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        return data;
    }
    
    /**
	 * 保存一个令牌的值，返回加密的字符。
	 * @param request
	 * @return
	 */
	public String SaveToken(HttpServletRequest request,String token_key)
	{
		AjaxTokenProcessor tokenObj = AjaxTokenProcessor.getInstance();
		return tokenObj.saveToken(request,token_key);
	}
	
	public String SaveToken(HttpServletRequest request)
	{
		AjaxTokenProcessor tokenObj = AjaxTokenProcessor.getInstance();
		return tokenObj.saveToken(request);
	}
	
	public String getTokenInputStr(HttpServletRequest request,String token_key){
		return SaveToken(request,token_key);
	}
	
	public String getTokenInputStr(HttpServletRequest request){
		return SaveToken(request);
	}

}
