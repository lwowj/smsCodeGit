package com.hero.sms.common.authentication;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserMenuService;
import com.hero.sms.service.organization.IOrganizationUserService;

/**
 * 自定义实现 ShiroRealm，包含认证和授权两大模块
 *
 * @author Administrator
 */
@Component
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private IOrganizationUserService organizationUserService;
    @Autowired
    private IOrganizationUserMenuService organizationUserMenuService;
    @Autowired
    private IOrganizationService organizationService;

    /**
     * 授权模块，获取用户角色和权限
     *
     * @param principal principal
     * @return AuthorizationInfo 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        OrganizationUser user = (OrganizationUser) SecurityUtils.getSubject().getPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        // 获取用户权限集
        List<OrganizationUserMenu> permissionList = this.organizationUserMenuService.findOrgUserPermissions(user.getUserAccount());
        Set<String> permissionSet = permissionList.stream().map(OrganizationUserMenu::getPerms).collect(Collectors.toSet());
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
        OrganizationUser user = this.organizationUserService.findByUserAccount(userName);

        if (user == null){
            throw new UnknownAccountException("账号未注册！");
        }else{
            Organization organization = this.organizationService.queryOrgByCode(user.getOrganizationCode());
            if(!OrgApproveStateEnums.SUCCESS.getCode().equals(organization.getApproveStateCode())) {
                throw new LockedAccountException("商户未认证，无法登录！");
            }
            if(OrgStatusEnums.Lock.getCode().equals(organization.getStatus())) {
                throw new LockedAccountException("商户已被锁定，无法登录！");
            }
            if(OrgStatusEnums.Cancel.getCode().equals(organization.getStatus())) {
                throw new LockedAccountException("商户已作废，无法登录！");
            }
            if (OrgStatusEnums.Lock.getCode().equals(user.getStatus())) {
                throw new LockedAccountException("账号已被锁定，无法登录！");
            }
            if (!StringUtils.equals(password, user.getUserPassword())){
                user.setLoginFaildCount(user.getLoginFaildCount() + 1);
                Integer faildCount = DatabaseCache.getCodeBySortCodeAndCode("System","loginFaildCount") == null ? 3 :
                        Integer.parseInt(DatabaseCache.getCodeBySortCodeAndCode("System","loginFaildCount").getName());
                if(user.getLoginFaildCount() >= faildCount){
                    user.setStatus(OrgStatusEnums.Lock.getCode());
                }
                this.organizationUserService.updateById(user);
                throw new IncorrectCredentialsException("用户名或密码错误！");
            }
            user.setLoginFaildCount(0);
            this.organizationUserService.updateById(user);
            OrganizationUserExt userExt = new OrganizationUserExt();
            BeanUtils.copyProperties(user,userExt);
            userExt.setOrganization(organization);
            return new SimpleAuthenticationInfo(userExt, password, getName());
        }
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
