package com.hero.sms.service.organization;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商户用户 Service接口
 *
 * @author Administrator
 * @date 2020-03-07 21:36:18
 */
public interface IOrganizationUserService extends IService<OrganizationUser> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationUser organizationUser
     * @return IPage<OrganizationUser>
     */
    IPage<OrganizationUser> findOrganizationUsers(QueryRequest request, OrganizationUser organizationUser);

    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationUser organizationUser
     * @return IPage<OrganizationUser>
     */
    IPage<OrganizationUser> selectLeftOrganizationPage(QueryRequest request, OrganizationUser organizationUser, Agent agent);

    /**
     * 查询（所有）
     *
     * @param organizationUser organizationUser
     * @return List<OrganizationUser>
     */
    List<OrganizationUser> findOrganizationUsers(OrganizationUser organizationUser);

    /**
     * 代理端商户用户新增
     * @param organizationUser
     * @param agent
     */
    @Transactional
    void createOrganizationUser(OrganizationUser organizationUser, Agent agent);

    /**
     * 新增
     *
     * @param organizationUser organizationUser
     */
    void createOrganizationUser(OrganizationUser organizationUser);

    /**
     * 代理端商户修改
     * @param organizationUser
     * @param agent
     */
    @Transactional
    void updateOrganizationUser(OrganizationUser organizationUser, Agent agent);

    /**
     * 修改
     *
     * @param organizationUser organizationUser
     */
    void updateOrganizationUser(OrganizationUser organizationUser);

    /**
     * 商户端修改用户
     * @param organizationUser
     * @param userExt
     */
    @Transactional
    void updateOrganizationUser(OrganizationUser organizationUser, OrganizationUserExt userExt);

    /**
     * 删除
     *
     * @param organizationUser organizationUser
     */
    void deleteOrganizationUser(OrganizationUser organizationUser);

    /**
    * 删除
    *
    * @param organizationUserIds organizationUserIds
    */
    void deleteOrganizationUsers(String[] organizationUserIds);

    /**
     * 通过用户名查找商户用户
     *
     * @param userAccount 用户名
     */
    OrganizationUser findByUserAccount(String userAccount);

    /**
     * 解锁、锁定
     */
    void updateUserStatus(String[] organizationUserIds, String status);

    /**
     * 强制、非强制google口令
     */
    void updateUserNeedGoogleStatus(String[] organizationUserIds, String status);
    
    /**
     * 代理解锁、锁定
     */
    void updateUserStatus(String[] organizationUserIds, String status,Agent agent);
    /**
     * 商户后台修改状态
     * @param organizationUserIds
     * @param status
     * @param organization
     */
    @Transactional
    void updateUserStatus(String[] organizationUserIds, String status, OrganizationUserExt organization);

    /**
     * 重置密码
     *
     * @param userAccounts 用户名数组
     */
    void resetPassword(String[] userAccounts);

    /**
     * 重置Google口令
     *
     * @param userAccounts 用户名数组
     */
    void resetGoogleKey(String[] userAccounts);
    
    /**
     * 商户端重置密码
     * @param userAccounts
     * @param organization
     */
    @Transactional
    void resetPassword(String[] userAccounts, OrganizationUserExt organization);

    /**
     * 修改密码
     *
     * @param userAccount 登录名
     * @param password 新密码
     */
    void updatePassword(String userAccount, String password);

    /**
     * 更新用户登录时间
     *
     * @param userAccount 登录名
     */
    void updateLoginTime(String userAccount);

    /**
     * 检验谷歌验证码
     * @param useraccount
     * @param verifyCode
     */
    void checkGoogleKey(String useraccount, String verifyCode);

    /**
     * 绑定谷歌
     * @param organizationUser
     * @param goologoVerifyCode
     * @param googleKey
     */
    void bindGoogle(OrganizationUser organizationUser, String goologoVerifyCode, String googleKey,String password);

    /**
     * 清除商户谷歌Key
     * @param organizationUser
     */
    void removeGoogleKey(OrganizationUser organizationUser, String goologoVerifyCode,String password);
}
