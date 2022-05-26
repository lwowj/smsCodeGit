package com.hero.sms.common.utils;


import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.entity.agent.Agent;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;


/**
 * FEBS工具类
 *
 * @author Administrator
 */
@Slf4j
public class FebsUtil {

    /**
     * 获取当前登录用户
     *
     * @return User
     */
    public static Agent getCurrentUser() {
        return (Agent) SecurityUtils.getSubject().getPrincipal();
    }

    public static String view(String viewName) {
        return FebsConstant.VIEW_PREFIX + viewName;
    }
}
