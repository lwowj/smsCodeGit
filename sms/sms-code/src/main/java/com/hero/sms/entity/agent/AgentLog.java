package com.hero.sms.entity.agent;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代理操作日志表 Entity
 *
 * @author Administrator
 * @date 2020-04-02 11:01:33
 */
@Data
@TableName("t_agent_log")
public class AgentLog {

    /**
     * 日志ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户
     */
    @TableField("USERNAME")
    private String username;

    /**
     * 操作内容
     */
    @TableField("OPERATION")
    private String operation;

    /**
     * 耗时
     */
    @TableField("TIME")
    private Long time;
    /**
     * 操作方法
     */
    @TableField("METHOD")
    private String method;

    /**
     * 方法参数
     */
    @TableField("PARAMS")
    private String params;

    /**
     * 操作者IP
     */
    @TableField("IP")
    private String ip;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 操作地点
     */
    @TableField("location")
    private String location;

}
