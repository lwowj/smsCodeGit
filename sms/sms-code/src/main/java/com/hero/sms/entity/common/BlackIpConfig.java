package com.hero.sms.entity.common;


import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 *  Entity
 *
 * @author Administrator
 * @date 2020-12-21 19:43:55
 */
@Data
@TableName("t_black_ip_config")
public class BlackIpConfig {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * IP
     */
    @TableField("Black_Ip")
    private String blackIp;

    /**
     * 限制项目权限 ALL所有 ZH总后台  AGENT代理  MERCH商户  GATEWAY网关
     */
    @TableField("LImit_Project")
    private String limitProject;

    /**
     * 是否可用:  1:可用，0为不可用;默认为1
     */
    @TableField("IsAvailability")
    private String isavailability;

    /**
     * 备注
     */
    @TableField("Remark")
    private String remark;

    /**
     * 描述
     */
    @TableField("Description")
    private String description;

    /**
     * 创建用户
     */
    @TableField("Create_User")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField("Create_Date")
    private Date createDate;

}
