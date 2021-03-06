package com.hero.sms.common.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.service.ISessionService;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class SessionServiceImpl implements ISessionService {

    @Autowired
    private SessionDAO sessionDAO;

    @Override
    public void forceLogoutOrg(String orgIds, Integer agentId){
        List<String> ids = Arrays.asList(orgIds.split(StringPool.COMMA));
        //商户平台的database=2,如配置更改,这里要相应的做更改
        RedisSessionDAO merchSessionDao = getDatabseRedisSessionDAO(2);
        Collection<Session> sessions = merchSessionDao.getActiveSessions();
        for (Session session : sessions) {
            OrganizationUserExt user;
            SimplePrincipalCollection principalCollection;
            if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                continue;
            } else {
                principalCollection = (SimplePrincipalCollection) session
                        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                user = (OrganizationUserExt) principalCollection.getPrimaryPrincipal();
                if (agentId.intValue() == user.getOrganization().getAgentId().intValue()
                        && ids.contains(String.valueOf(user.getOrganization().getId()))
                        && session.getTimeout() != 0L){
                    //是所属商户用户 并且在线的 全部踢下线(只能踢掉自己代理的用户)
                    session.setTimeout(0);
                    session.stop();
                    merchSessionDao.delete(session);
                    log.info(String.format("商户用户【%s】已于【%s】被踢下线",user.getUserAccount()
                            , DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN)));
                }
            }
        }
    }

    @Override
    public void forceLogoutOrgUser(String orgUserIds, Integer agentId){
        List<String> ids = Arrays.asList(orgUserIds.split(StringPool.COMMA));
        //商户平台的database=2,如配置更改,这里要相应的做更改
        RedisSessionDAO merchSessionDao = getDatabseRedisSessionDAO(2);
        Collection<Session> sessions = merchSessionDao.getActiveSessions();
        for (Session session : sessions) {
            OrganizationUserExt user;
            SimplePrincipalCollection principalCollection;
            if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                continue;
            } else {
                principalCollection = (SimplePrincipalCollection) session
                        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                user = (OrganizationUserExt) principalCollection.getPrimaryPrincipal();
                if (agentId.intValue() == user.getOrganization().getAgentId().intValue()
                        && ids.contains(String.valueOf(user.getId()))
                        && session.getTimeout() != 0L){
                    //是要提出的用户 并且在线的 全部踢下线
                    session.setTimeout(0);
                    session.stop();
                    merchSessionDao.delete(session);
                    log.info(String.format("商户用户【%s】已于【%s】被踢下线",user.getUserAccount()
                            ,DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN)));
                }
            }
        }
    }

    @Override
    public void forceLogoutOrgByOrgCodes(String orgCodes, Integer agentId) {
        List<String> codes = Arrays.asList(orgCodes.split(StringPool.COMMA));
        //商户平台的database=2,如配置更改,这里要相应的做更改
        RedisSessionDAO merchSessionDao = getDatabseRedisSessionDAO(2);
        Collection<Session> sessions = merchSessionDao.getActiveSessions();
        for (Session session : sessions) {
            OrganizationUserExt user;
            SimplePrincipalCollection principalCollection;
            if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                continue;
            } else {
                principalCollection = (SimplePrincipalCollection) session
                        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                user = (OrganizationUserExt) principalCollection.getPrimaryPrincipal();
                if (agentId.intValue() == user.getOrganization().getAgentId().intValue()
                        && codes.contains(user.getOrganizationCode())
                        && session.getTimeout() != 0L){
                    //是要提出的用户 并且在线的 全部踢下线
                    session.setTimeout(0);
                    session.stop();
                    merchSessionDao.delete(session);
                    log.info(String.format("商户用户【%s】已于【%s】被踢下线",user.getUserAccount()
                            ,DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN)));
                }
            }
        }
    }

    @Override
    public void forceLogoutAgent(Integer agentId, Integer upAgentId){
        if (agentId == null) return;
        //代理平台的database=1，如代理平台的redis配置有做更改,这里要相应的做更改
        RedisSessionDAO merchSessionDao = getDatabseRedisSessionDAO(1);
        Collection<Session> sessions = merchSessionDao.getActiveSessions();
        for (Session session : sessions) {
            Agent user;
            SimplePrincipalCollection principalCollection;
            if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                continue;
            } else {
                principalCollection = (SimplePrincipalCollection) session
                        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                user = (Agent) principalCollection.getPrimaryPrincipal();
                if (user.getUpAgentId() != null
                        && upAgentId.intValue() == user.getUpAgentId().intValue()
                        && agentId.intValue() == user.getId().intValue()
                        && session.getTimeout() != 0L){
                    //是要提出的用户 并且在线的 全部踢下线
                    session.setTimeout(0);
                    session.stop();
                    merchSessionDao.delete(session);
                    log.info(String.format("代理【%s】已于【%s】被踢下线",user.getAgentName()
                            ,DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN)));
                }
            }
        }
    }
    /**
     * 获取database对应的 RedisSessionDao
     * @param database  redis库序号
     * @return
     */
    private RedisSessionDAO getDatabseRedisSessionDAO(int database){
        RedisSessionDAO merchSessionDao = new RedisSessionDAO();
        RedisManager redisManager = new RedisManager();
        RedisManager existManage = (RedisManager) ((RedisSessionDAO)sessionDAO).getRedisManager();
        redisManager.setHost(existManage.getHost());
        redisManager.setPassword(existManage.getPassword());
        redisManager.setTimeout(existManage.getTimeout());
        redisManager.setDatabase(database);
        merchSessionDao.setRedisManager(redisManager);
        return merchSessionDao;
    }
}
