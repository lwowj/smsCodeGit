package com.hero.sms.entity.channel;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 通道资费 Entity
 *
 * @author Administrator
 * @date 2020-03-10 15:27:23
 */
@Data
@TableName("t_sms_channel_cost")
public class SmsChannelCost {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 通道ID
     */
    @TableField("sms_channel_id")
    private Integer smsChannelId;

    /**
     * 短信类型
     */
    @TableField("sms_type")
    private Integer smsType;

    /**
     * 资费名称（国内、国际）
     */
    @TableField("name")
    private String name;

    /**
     * 运营商
     * 电信 CTCC
     * 联通 CUCC
     * 移动 CMCC
     */
    @TableField("Operator")
    private String operator;
    
    /**
     * 资费
     */
    @TableField("value")
    private String value;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

}
