package com.hero.sms.common.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.service.ISessionService;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SessionServiceImpl implements ISessionService {

    @Autowired
    private SessionDAO sessionDAO;

    @Override
    public void forceLogoutOrgUser(String orgUserIds, String orgCode){
        List<String> ids = Arrays.asList(orgUserIds.split(StringPool.COMMA));
        Collection<Session> sessions = sessionDAO.getActiveSessions();
        for (Session session : sessions) {
            OrganizationUserExt user;
            SimplePrincipalCollection principalCollection;
            if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                continue;
            } else {
                principalCollection = (SimplePrincipalCollection) session
                        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                user = (OrganizationUserExt) principalCollection.getPrimaryPrincipal();
                if (user.getOrganizationCode().equals(orgCode)
                        && ids.contains(String.valueOf(user.getId()))
                        && session.getTimeout() != 0L){
                    //是要提出的用户 并且在线的 全部踢下线
                    session.setTimeout(0);
                    session.stop();
                    sessionDAO.delete(session);
                    log.info(String.format("商户用户【%s】已于【%s】被踢下线",user.getUserAccount()
                            , DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN)));
                }
            }
        }
    }
}
