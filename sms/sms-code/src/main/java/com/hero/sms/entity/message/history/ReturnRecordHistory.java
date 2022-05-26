package com.hero.sms.entity.message.history;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 历史回执记录 Entity
 *
 * @author Administrator
 * @date 2020-03-15 23:31:41
 */
@Data
@TableName("t_return_record_history")
public class ReturnRecordHistory {

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
     * 代理商id
     */
    @TableField("agent_id")
    private Integer agentId;

    /**
     * 
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
     * 手机号码归属地
     */
    @TableField("sms_number_area")
    private String smsNumberArea;

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
    @TableField("req_state")
    private Integer reqState;

    /**
     * 通道请求返回信息
     */
    @TableField("req_desc")
    private String reqDesc;

    /**
     * 提交时间
     */
    @TableField("req_create_time")
    private Date reqCreateTime;

    /**
     * 通道消息标识
     */
    @TableField("res_msgid")
    private String resMsgid;

    /**
     * 接收状态
     */
    @TableField("return_state")
    private Integer returnState;

    /**
     * 接收状态描述
     */
    @TableField("return_desc")
    private String returnDesc;

    /**
     * 消息序列号
     */
    @TableField("return_seq")
    private String returnSeq;

    /**
     * 创建时间
     */
    @TableId(value = "create_time", type = IdType.AUTO)
    private Date createTime;

}
