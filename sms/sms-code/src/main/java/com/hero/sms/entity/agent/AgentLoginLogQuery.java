package com.hero.sms.entity.agent;


import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * 代理登录日志表 Entity 扩展
 *
 * @author Administrator
 * @date 2020-12-25 17:32:55
 */
@Data
public class AgentLoginLogQuery  extends AgentLoginLog  {

    /**
     * 用户名（模糊查询）
     */
    private String agentAccountFuzzy;

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
