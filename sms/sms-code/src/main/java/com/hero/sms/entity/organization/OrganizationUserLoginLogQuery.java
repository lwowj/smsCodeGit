package com.hero.sms.entity.organization;


import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * 商户用户登录日志表 Entity扩展
 *
 * @author Administrator
 * @date 2020-12-25 17:33:01
 */
@Data
public class OrganizationUserLoginLogQuery  extends OrganizationUserLoginLog  {

    /**
     * 用户名（模糊查询）
     */
    private String userAccountFuzzy;

    /**
     * 操作ip（模糊查询）
     */
    private String localIpFuzzy;
    
    /**
     * 提交开始时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createStartTime;

    /**
     * 提交结束时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createEndTime;
    
    /**
     * 排序方式
     */
    private String orderByCreateTimeWay;

}
