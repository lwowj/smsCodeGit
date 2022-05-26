package com.hero.sms.entity.channel;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 短信通道属性 Entity
 *
 * @author Administrator
 * @date 2020-03-08 17:35:16
 */
@Data
@TableName("t_sms_channel_property")
public class SmsChannelProperty {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 通道ID
     */
    @TableField("sms_channel_id")
    private Integer smsChannelId;
    
    /**
     * 属性类型
     */
    @TableField("property_type")
    private Integer propertyType;

    /**
     * 属性名
     */
    @TableField("name")
    private String name;

    /**
     * 属性值
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
     * 创建日期
     */
    @TableField("create_time")
    private Date createTime;

}
