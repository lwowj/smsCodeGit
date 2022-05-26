package com.hero.sms.entity.rechargeOrder;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代理充值订单 Entity
 *
 * @author Administrator
 * @date 2020-03-17 21:37:47
 */
@Data
@TableName("t_agent_recharge_order")
public class AgentRechargeOrder {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代理id
     */
    @TableField("agent_id")
    private String agentId;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 通道id
     */
    @TableField("pay_channel_id")
    private String payChannelId;

    /**
     * 通道商户号
     */
    @TableField("pay_merch_no")
    private String payMerchNo;

    /**
     * 充值类型(网银、平台充值、代理充值)
     */
    @TableField("recharge_type")
    private String rechargeType;

    /**
     * 充值方式
     */
    @TableField("netway_code")
    private String netwayCode;

    /**
     * 充值凭证
     */
    @TableField("recharge_picture_url")
    private String rechargePictureUrl;

    /**
     * 充值金额(分)
     */
    @TableField("recharge_amount")
    private Integer rechargeAmount;

    /**
     * 可用金额(分)
     */
    @TableField("available_amount")
    private Integer availableAmount;

    /**
     * 请求状态(成功、失败、签名错误、其它错误)
     */
    @TableField("req_state_code")
    private String reqStateCode;

    /**
     * 请求时间
     */
    @TableField("req_time")
    private String reqTime;

    /**
     * 请求状态描述
     */
    @TableField("req_state_description")
    private String reqStateDescription;

    /**
     * 充值状态(成功、失败、未知、未支付)
     */
    @TableField("recharge_state_code")
    private String rechargeStateCode;

    /**
     * 充值时间
     */
    @TableField("recharge_time")
    private String rechargeTime;

    /**
     * 充值完成时间
     */
    @TableField("recharge_complete_time")
    private String rechargeCompleteTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField("ceate_user")
    private String ceateUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 入账方式
     * 是否挂账（0入账1挂账）
     * 2021-04-07
     */
    @TableField("recorded_type")
    private String recordedType;
}
