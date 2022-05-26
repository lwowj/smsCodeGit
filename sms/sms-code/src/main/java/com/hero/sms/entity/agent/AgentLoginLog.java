package com.hero.sms.entity.agent;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代理登录日志表 Entity
 *
 * @author Administrator
 * @date 2020-12-25 17:32:55
 */
@Data
@TableName("t_agent_login_log")
public class AgentLoginLog {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    @TableField("Agent_Account")
    private String agentAccount;

    /**
     * 当前的ip
     */
    @TableField("Local_Ip")
    private String localIp;

    /**
     * 登录状态：1成功 0失败
     */
    @TableField("Login_State")
    private Integer loginState;

    /**
     * 返回信息
     */
    @TableField("Message")
    private String message;

    /**
     * 登录耗时
     */
    @TableField("TimeConsuming")
    private Long timeConsuming;
    /**
     * 创建时间
     */
    @TableField("Create_Time")
    private Date createTime;

}
