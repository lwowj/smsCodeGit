package com.hero.sms.entity.agent;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户代理资费 Entity
 *
 * @author Administrator
 * @date 2020-03-06 10:05:33
 */
@Data
@TableName("t_agent_cost")
public class AgentCost {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代理ID
     */
    @TableField("Agent_Id")
    private Integer agentId;
    
    @TableField("sms_type")
    private Integer smsType;

    /**
     * 属性名
     */
    @TableField("Name")
    private String name;

    /**
     * 属性值
     */
    @TableField("Value")
    private String value;

    /**
     * 属性值
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
