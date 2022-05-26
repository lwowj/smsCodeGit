package com.hero.sms.entity.channel;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 支付通道 Entity
 *
 * @author Administrator
 * @date 2020-03-12 11:02:02
 */
@Data
@TableName("t_pay_channel")
public class PayChannel {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 通道名称
     */
    @TableField("channel_name")
    private String channelName;

    /**
     * 商户号
     */
    @TableField("merch_no")
    private String merchNo;

    /**
     * 网关类型
     */
    @TableField("netway_code")
    private String netwayCode;

    /**
     * 实现类
     */
    @TableField("impl_full_class")
    private String implFullClass;

    /**
     * 权重
     */
    @TableField("weight")
    private Integer weight;

    /**
     * 通道成本
     */
    @TableField("cost")
    private String cost;

    /**
     * 最小金额
     */
    @TableField("min_amount")
    private Integer minAmount;

    /**
     * 最大金额
     */
    @TableField("max_amount")
    private Integer maxAmount;

    /**
     * 提交url
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 回调url
     */
    @TableField("callBack_url")
    private String callbackUrl;

    /**
     * 交易编码
     */
    @TableField("pay_code")
    private String payCode;

    /**
     * 签名秘钥
     */
    @TableField("sign_key")
    private String signKey;

    /**
     * 安全秘钥
     */
    @TableField("encrypt_key")
    private String encryptKey;

    /**
     * 通道状态
     */
    @TableField("state_code")
    private String stateCode;

    /**
     * KE1
     */
    @TableField("key1")
    private String key1;

    /**
     * KE2
     */
    @TableField("key2")
    private String key2;

    /**
     * KE3
     */
    @TableField("key3")
    private String key3;

    /**
     * KE4
     */
    @TableField("key4")
    private String key4;

    /**
     * KE5
     */
    @TableField("key5")
    private String key5;

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
