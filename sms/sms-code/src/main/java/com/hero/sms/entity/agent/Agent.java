package com.hero.sms.entity.agent;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户代理 Entity
 *
 * @author Administrator
 * @date 2020-03-22 21:16:46
 */
@Data
@TableName("t_agent")
public class Agent implements Serializable {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 上级代理ID
     */
    @TableField("Up_Agent_Id")
    private Integer upAgentId;

    /**
     * 
     */
    @TableField("Agent_Name")
    private String agentName;

    /**
     * 用户名
     */
    @TableField("Agent_Account")
    private String agentAccount;

    /**
     * 密码
     */
    @TableField("Agent_Password")
    private String agentPassword;

    /**
     * 支付密码
     */
    @TableField("Pay_Password")
    private String payPassword;

    /**
     * 上次登录IP地址
     */
    @TableField("Last_Login_IP")
    private String lastLoginIp;

    /**
     * 上次登录时间
     */
    @TableField("Last_Login_Time")
    private String lastLoginTime;

    /**
     * 登录失败次数
     */
    @TableField("Login_Faild_Count")
    private Integer loginFaildCount;

    /**
     * 充值总额
     */
    @TableField("Amount")
    private Long amount;

    /**
     * 可用额度
     */
    @TableField("Quota_Amount")
    private Long quotaAmount;

    /**
     * 可用金额(利润）
     */
    @TableField("Available_Amount")
    private Long availableAmount;

    /**
     * 消费金额
     */
    @TableField("Cash_Amount")
    private Long cashAmount;

    /**
     * 发送短信数量
     */
    @TableField("Send_Sms_Total")
    private Long sendSmsTotal;

    /**
     * 日限量
     */
    @TableField("Day_Limit")
    private Long dayLimit;

    /**
     * 用户状态（正常，锁定等）
     */
    @TableField("State_Code")
    private String stateCode;

    /**
     * 手机号
     */
    @TableField("Phone_Number")
    private String phoneNumber;

    /**
     * 邮箱
     */
    @TableField("EMail")
    private String email;

    /**
     * 
     */
    @TableField("QQ")
    private String qq;

    /**
     * 代理级别
     */
    @TableField("Level_Code")
    private String levelCode;

    /**
     * 防篡改码
     */
    @TableField("Data_MD5")
    private String dataMd5;

    /**
     * 谷歌秘钥
     */
    @TableField("Google_Key")
    private String googleKey;

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

}
