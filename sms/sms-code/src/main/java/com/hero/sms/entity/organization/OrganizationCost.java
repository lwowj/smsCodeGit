package com.hero.sms.entity.organization;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户用户资费 Entity
 *
 * @author Administrator
 * @date 2020-03-08 00:12:30
 */
@Data
@TableName("t_organization_cost")
public class OrganizationCost {

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
    
    @TableField("sms_type")
    private Integer smsType;

    /**
     * 资费名称（国内、国际）
     */
    @TableField("Cost_Name")
    private String costName;

    /**
     * 资费费率
     */
    @TableField("Cost_Value")
    private String costValue;

    /**
     * 状态
     */
    @TableField("State")
    private String state;

    /**
     * 通道Id
     */
    @TableField("Channel_Id")
    private String channelId;

    /**
     * 运营商
     * 电信 CTCC
     * 联通 CUCC
     * 移动 CMCC
     */
    @TableField("Operator")
    private String operator;
    
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
     * 创建时间
     */
    @TableField("Create_Date")
    private Date createDate;

}
