package com.hero.sms.entity.organization;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hero.sms.common.converter.TimeConverter;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

/**
 * 商户用户 Entity
 *
 * @author Administrator
 * @date 2020-03-22 15:54:28
 */
@Data
@TableName("t_organization_user")
public class OrganizationUser implements Serializable {

    private static final long serialVersionUID = -8223508582965617811L;
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
     * 真实姓名
     */
    @TableField("User_Name")
    private String userName;

    /**
     * 登录账号
     */
    @TableField("User_Account")
    private String userAccount;

    /**
     * 密码
     */
    @TableField("User_Password")
    private String userPassword;

    /**
     * 上次登录IP地址
     */
    @TableField("Last_Login_IP")
    private String lastLoginIp;

    /**
     * 上次登录时间
     */
    @TableField("Last_Login_Time")
    @ExcelField(value = "上次登录时间", writeConverter = TimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 登录失败次数
     */
    @TableField("Login_Faild_Count")
    private Integer loginFaildCount;

    /**
     * 状态（正常，锁定等）
     */
    @TableField("Status")
    private String status;

    /**
     * 账号类型(主账户、子账户)
     */
    @TableField("Account_Type")
    private String accountType;

    /**
     * 谷歌秘钥
     */
    @TableField("Google_Key")
    private String googleKey;

    /**
         * 是否强制绑定谷歌秘钥
     */
    @TableField("Need_Bind_Google_Key")
    private Integer needBindGoogleKey;

    /**
         * 本次次登录IP地址
     */
    @TableField("Login_IP")
    private String loginIp;
    
    /**
     * 描述
     */
    @TableField("Description")
    private String description;
    
    /**
     * 备注
     */
    @TableField("Remark")
    private String remark;

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
