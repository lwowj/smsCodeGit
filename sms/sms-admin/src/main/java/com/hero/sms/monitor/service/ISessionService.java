package com.hero.sms.monitor.service;

import com.hero.sms.monitor.entity.ActiveUser;

import java.util.List;

/**
 * @author Administrator
 */
public interface ISessionService {

    /**
     * 获取在线用户列表
     *
     * @param username 用户名
     * @return List<ActiveUser>
     */
    List<ActiveUser> list(String username);

    /**
     * 踢出用户
     *
     * @param sessionId sessionId
     */
    void forceLogout(String sessionId);

    /**
     * 踢出所有（id in orgIds）商户已登录的用户
     * @param orgIds 商户ID列表，以逗号隔开
     */
    void forceLogoutOrg(String orgIds);
    /**
     * 踢出所有（id in orgUserIds）的商户用户
     * @param orgUserIds 商户用户ID列表，以逗号隔开
     */
    void forceLogoutOrgUser(String orgUserIds);

    /**
     * 踢出所有（orgCode in orgCodes）商户已登录的用户
     * @param orgCodes 商户ID列表，以逗号隔开
     */
    void forceLogoutOrgByOrgCodes(String orgCodes);
    /**
     * 踢出代理用户
     * @param agentId
     */
    void forceLogoutAgent(Integer agentId);
}
