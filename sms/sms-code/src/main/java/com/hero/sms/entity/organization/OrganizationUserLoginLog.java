package com.hero.sms.entity.organization;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户用户登录日志表 Entity
 *
 * @author Administrator
 * @date 2020-12-25 17:33:01
 */
@Data
@TableName("t_organization_user_login_log")
public class OrganizationUserLoginLog {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户编号
     */
    @TableField("Organization_Code")
    private String organizationCode;

    /**
     * 登录账号
     */
    @TableField("User_Account")
    private String userAccount;

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
