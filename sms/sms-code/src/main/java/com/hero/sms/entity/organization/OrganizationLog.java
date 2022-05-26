package com.hero.sms.entity.organization;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户操作日志表 Entity
 *
 * @author Administrator
 * @date 2020-03-21 23:29:41
 */
@Data
@TableName("t_organization_log")
public class OrganizationLog {

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
