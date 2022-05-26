package com.hero.sms.common.util;


import com.hero.sms.common.entity.FebsConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * FEBS工具类
 *
 * @author Administrator
 */
@Slf4j
public class FebsUtil {

    public static String view(String viewName) {
        return FebsConstant.VIEW_PREFIX + viewName;
    }
}
