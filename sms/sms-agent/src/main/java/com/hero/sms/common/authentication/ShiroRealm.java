package com.hero.sms.common.authentication;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenu;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.hero.sms.service.agent.IAgentMenuService;
import com.hero.sms.service.agent.IAgentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义实现 ShiroRealm，包含认证和授权两大模块
 *
 * @author Administrator
 */
@Component
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private IAgentService agentService;
    @Autowired
    private IAgentMenuLimitService agentMenuLimitService;
    @Autowired
    private IAgentMenuService agentMenuService;

    /**
     * 授权模块，获取用户角色和权限
     *
     * @param principal principal
     * @return AuthorizationInfo 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        Agent agent = (Agent) SecurityUtils.getSubject().getPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        // 获取用户权限集
        List<AgentMenu> permissionList = this.agentMenuService.findAgentPermissions(agent.getAgentAccount());
        Set<String> permissionSet = permissionList.stream().map(AgentMenu::getPerms).collect(Collectors.toSet());
        simpleAuthorizationInfo.setStringPermissions(permissionSet);
        return simpleAuthorizationInfo;
    }

    /**
     * 用户认证
     *
     * @param token AuthenticationToken 身份认证 token
     * @return AuthenticationInfo 身份认证信息
     * @throws AuthenticationException 认证相关异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 获取用户输入的用户名和密码
        String userName = (String) token.getPrincipal();
        String password = new String((char[]) token.getCredentials());

        // 通过用户名到数据库查询用户信息
        Agent agent = this.agentService.findByAccount(userName);

        if (agent == null)
            throw new UnknownAccountException("账号未注册！");
        if (OrgStatusEnums.Lock.getCode().equals(agent.getStateCode()))
            throw new LockedAccountException("账号已被锁定,请联系管理员！");
        if (!StringUtils.equals(password, agent.getAgentPassword())) {
            agent.setLoginFaildCount(agent.getLoginFaildCount() + 1);
            Integer faildCount = DatabaseCache.getCodeBySortCodeAndCode("System","loginFaildCount") == null ? 3 :
                    Integer.parseInt(DatabaseCache.getCodeBySortCodeAndCode("System","loginFaildCount").getName());
            if(agent.getLoginFaildCount() >= faildCount){
                agent.setStateCode(OrgStatusEnums.Lock.getCode());
            }
            this.agentService.updateById(agent);
            throw new IncorrectCredentialsException("用户名或密码错误！");
        }
        agent.setLoginFaildCount(0);
        this.agentService.updateById(agent);
        return new SimpleAuthenticationInfo(agent, password, getName());
    }

    /**
     * 清除当前用户权限缓存
     * 使用方法：在需要清除用户权限的地方注入 ShiroRealm,
     * 然后调用其 clearCache方法。
     */
    public void clearCache() {
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        super.clearCache(principals);
    }
}
