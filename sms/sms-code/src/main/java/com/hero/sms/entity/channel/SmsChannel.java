package com.hero.sms.entity.channel;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 短信通道 Entity
 *
 * @author Administrator
 * @date 2020-03-08 17:35:03
 */
@Data
@TableName("t_sms_channel")
public class SmsChannel {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField(value = "code")
    private String code;

    /**
     * 通道名称
     */
    @TableField("name")
    private String name;

    /**
     * 状态
     */
    @TableField("state")
    private Integer state;
    
    @TableField("support_area")
    private String supportArea;

    /**
     * 协议类型
     */
    @TableField("protocol_type")
    private String protocolType;

    /**
     * 实现类
     */
    @TableField("impl_full_class")
    private String implFullClass;

    /**
     * 地区限制
     */
    @TableField("area_regex")
    private String areaRegex;
    
    /**
     * 运营商限制
     */
    @TableField("operator_regex")
    private String operatorRegex;
    
    /**
     * 地区运营商组合限制
     */
    @TableField("area_operator_regex")
    private String areaOperatorRegex;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 数值越高，权重越大
     */
    @TableField("weight")
    private Integer weight;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否删除 0否 1是
     */
    @TableField("is_del")
    private Integer isDel;

    /**
     * 创建人
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 每日限量
     */
    @TableField("day_limit")
    private Long dayLimit;

    /**
     * 创建日期
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 状态与
     */
    @TableField(exist = false)
    private Integer stateWith;
    
    /**
     * 提交方式  0同步 1异步
     */
    @TableField("submit_way")
    private Integer submitWay;
}
