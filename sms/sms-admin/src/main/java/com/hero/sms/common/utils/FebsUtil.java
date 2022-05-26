package com.hero.sms.common.utils;


import org.apache.shiro.SecurityUtils;

import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.system.entity.User;

import lombok.extern.slf4j.Slf4j;

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
    public static User getCurrentUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }

    public static String view(String viewName) {
        return FebsConstant.VIEW_PREFIX + viewName;
    }
}
