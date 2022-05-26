package com.hero.sms.common.service;

/**
 * @author Administrator
 */
public interface ISessionService {

    /**
     * 踢出所有（id in orgIds）商户已登录的用户
     * @param orgIds 商户ID列表，以逗号隔开
     * @param id
     */
    void forceLogoutOrg(String orgIds, Integer agentId);
    /**
     * 踢出所有（id in orgUserIds）的商户用户
     * @param orgUserIds 商户用户ID列表，以逗号隔开
     */
    void forceLogoutOrgUser(String orgUserIds, Integer agentId);

    /**
     * 踢出所有（orgCode in orgCodes）商户已登录的用户
     * @param orgCodes 商户ID列表，以逗号隔开
     */
    void forceLogoutOrgByOrgCodes(String orgCodes, Integer agentId);
    /**
     * 踢出代理用户
     * @param agentId
     */
    void forceLogoutAgent(Integer agentId, Integer upAgentId);
}
