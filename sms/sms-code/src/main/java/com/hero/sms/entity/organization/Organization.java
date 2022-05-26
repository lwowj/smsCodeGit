package com.hero.sms.entity.organization;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户信息 Entity
 *
 * @author Administrator
 * @date 2020-03-08 16:47:16
 */
@Data
@TableName("t_organization")
public class Organization implements Serializable {

    private static final long serialVersionUID = -8109067913233204860L;
    /**
     *
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代理商ID
     */
    @TableField("Agent_Id")
    private Integer agentId;

    /**
     * 商务ID
     */
    @TableField("Business_User_Id")
    private Integer businessUserId;

    /**
	* 商户名称
     */
    @TableField("Organization_Name")
    private String organizationName;

    /**
     * 商户编号
     */
    @TableField("Organization_Code")
    private String organizationCode;

    /**
     * 联系人
     */
    @TableField("Contact")
    private String contact;

    /**
     * 联系电话
     */
    @TableField("Contact_Mobile")
    private String contactMobile;

    /**
     * 商户邮箱
     */
    @TableField("EMail")
    private String email;

    /**
     * 商户地址
     */
    @TableField("Address")
    private String address;

    /**
     * 商户网站
     */
    @TableField("Web_Url")
    private String webUrl;

    /**
     * 交易总额
     */
    @TableField("Amount")
    private Long amount;

    /**
     * 可用金额
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
     * 机构状态（正常，锁定等）
     */
    @TableField("Status")
    private String status;

    /**
     * 认证状态
     */
    @TableField("Approve_State_Code")
    private String approveStateCode;

    /**
     * 签名
     */
    @TableField("Sms_Sign")
    private String smsSign;

    /**
     * 计费类型
     */
    @TableField("charges_type")
    private String chargesType;

    /**
     * 结算类型
     */
    @TableField("Settlement_type")
    private String settlementType;

    /**
     * 短信审核类型
     */
    @TableField("Sms_Approve_type")
    private String smsApproveType;

    /**
     * 开通接口类型
     */
    @TableField("interface_type")
    private Integer interfaceType;

    /**
     * md5密钥
     */
    @TableField("Md5_Key")
    private String md5Key;

    /**
     * 绑定IP
     */
    @TableField("Bind_IP")
    private String bindIp;

    /**
     * 通知地址
     */
    @TableField("notify_url")
    private String notifyUrl;

    /**
     * 防篡改码
     */
    @TableField("Data_MD5")
    private String dataMd5;

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
