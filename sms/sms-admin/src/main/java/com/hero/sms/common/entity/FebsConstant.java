package com.hero.sms.common.entity;

/**
 * 常量
 *
 * @author Administrator
 */
public class FebsConstant {

    // 注册用户角色ID
    public static final Long REGISTER_ROLE_ID = 2L;

    // 前端页面路径前缀
    public static final String VIEW_PREFIX = "febs/views/";

    // 验证码 Session Key
    public static final String CODE_PREFIX = "febs_captcha_";

    // 允许下载的文件类型，根据需求自己添加（小写）
    public static final String[] VALID_FILE_TYPE = {"xlsx", "zip"};

    /**
     * {@link com.hero.sms.common.controller.BaseController}
     * getDataTable 中 HashMap 默认的初始化容量
     */
    public static final int DATA_MAP_INITIAL_CAPACITY = 4;
    /**
     * 异步线程池名称
     */
    public static final String ASYNC_POOL = "febsAsyncThreadPool";
}
