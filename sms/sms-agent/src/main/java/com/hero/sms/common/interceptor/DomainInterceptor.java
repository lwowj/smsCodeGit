package com.hero.sms.common.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.URLUtil;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.service.agent.IAgentSystemConfigService;
import com.hero.sms.utils.FileDownLoadUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Component
public class DomainInterceptor implements HandlerInterceptor {

    @Autowired
    private IAgentSystemConfigService agentSystemConfigService;

    //请求处理前，也就是访问Controller前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        //获取顶级域名
        String domainName = URLUtil.getDomainName(request.getRequestURL().toString());

        Map<String,String> map =(Map<String,String>) session.getAttribute("sysConfig");
        //配置已经存在  并且域名与配置中的域名一样  直接通过
        if ( map != null ){
            String domain = map.get("domain");
            if ( StringUtils.isNotBlank(domain) && domain.equals(domainName)){
                return true;
            }
        }
        if (domainName != null){
            LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AgentSystemConfig::getSystemUrl,domainName);
            queryWrapper.eq(AgentSystemConfig::getApproveStateCode, OrgApproveStateEnums.SUCCESS.getCode());
            AgentSystemConfig agentSystemConfig = agentSystemConfigService.getOne(queryWrapper);
            if (agentSystemConfig != null){
                /**
                 * @begin
                 * 2021-04-21
                 * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
                 * 【说明】：这里需要新增校验agentSystemConfig.getSystemLogoUrl() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
                 * 【步骤】：
                 * 1、使用文件工具判断文件地址的文件是否存在于本机上
                 * 2、获取字典中负载服务器的IP地址
                 * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
                 */
            	FileDownLoadUtil.downLoadAgentLogo(agentSystemConfig.getSystemLogoUrl());
                /**
                 * @end
                 */
                putConfig(session,agentSystemConfig.getSystemName(),agentSystemConfig.getSystemLogoUrl(),domainName);
                return true;
            }
        }
        String logoPath = DatabaseCache.getCodeBySortCodeAndCode("System", "logoPath").getName();
        String platformName = DatabaseCache.getCodeBySortCodeAndCode("System", "platformName").getName();
        putConfig(session,platformName,logoPath,domainName);
        return true;
    }

    //请求已经被处理，但在视图渲染前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    //请求结果结果已经渲染好了，response没有进行返回但也无法修改reponse了（一般用于清理数据）
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private void putConfig(HttpSession session,String platformName,String logoPath,String domain){
        Map<String,String> sysConfig = new HashMap<>();
        sysConfig.put("platformName",platformName);
        sysConfig.put("logoPath",logoPath);
        sysConfig.put("domain",domain);
        session.setAttribute("sysConfig",sysConfig);
    }
}
