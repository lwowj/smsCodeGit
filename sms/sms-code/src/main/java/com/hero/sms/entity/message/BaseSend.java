package com.hero.sms.entity.message;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class BaseSend implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4278680418343690118L;

	/**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 批次号
     */
    @TableField("send_code")
    private String sendCode;
    
    /**
     * 上级代理商id
     */
    @TableField("up_agent_id")
    private Integer upAgentId;

    /**
     * 代理商id
     */
    @TableField("agent_id")
    private Integer agentId;

    /**
     * 商户编号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 提交方式
     */
    @TableField("sub_type")
    private Integer subType;

    /**
     * 消息类型
     */
    @TableField("sms_type")
    private Integer smsType;

    /**
     * 消息内容
     */
    @TableField("sms_content")
    private String smsContent;

    /**
     * 手机号码归属地区
     */
    @TableField("sms_number_area")
    private String smsNumberArea;
	
	/**
     * 有效的短信数
     */
    @TableField("sms_count")
    private Integer smsCount;

    /**
     * 短信字数
     */
    @TableField("sms_words")
    private Integer smsWords;
    
    /**
     * 消费金额
     */
    @TableField("consume_amount")
    private Integer consumeAmount;

    /**
     * 通道成本
     */
    @TableField("channel_cost_amount")
    private Integer channelCostAmount;
    
    /**
     * 上级代理商收益
     */
    @TableField("up_agent_income_amount")
    private Integer upAgentIncomeAmount;

    /**
     * 代理商收益
     */
    @TableField("agent_income_amount")
    private Integer agentIncomeAmount;

    /**
     * 平台收益
     */
    @TableField("income_amount")
    private Integer incomeAmount;
}
