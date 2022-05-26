package com.hero.sms.entity.message.history;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 历史发送记录 Entity
 *
 * @author Administrator
 * @date 2020-03-15 23:31:38
 */
@Data
@TableName("t_send_record_history")
public class SendRecordHistory {

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
     * 通道id
     */
    @TableField("channel_id")
    private Integer channelId;

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
     * 手机号码
     */
    @TableField("sms_number")
    private String smsNumber;

    /**
     * 运营商
     */
    @TableField("sms_number_operator")
    private String smsNumberOperator;

    /**
     * 手机号码归属地区
     */
    @TableField("sms_number_area")
    private String smsNumberArea;

    /**
     * 手机号码归属地(省份)
     */
    @TableField("sms_number_province")
    private String smsNumberProvince;


    /**
     * 源号码
     */
    @TableField("source_number")
    private String sourceNumber;
    
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
     * 通道请求状态
     */
    @TableField("state")
    private Integer state;

    /**
     * 通道请求返回信息
     */
    @TableField("state_desc")
    private String stateDesc;

    /**
     * 通道消息标识
     */
    @TableField("res_msgid")
    private String resMsgid;
    
    /**
     * 回执时间
     */
    @TableField(value = "receipt_time")
    private Date receiptTime;

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

    /**
     * 创建时间
     */
    @TableId(value = "create_time")
    private Date createTime;

    /**
     * 2020-12-01 添加
         *  请求上游时间
     */
    @TableField("submit_time")
    private Date submitTime;
}
