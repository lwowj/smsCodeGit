package com.hero.sms.entity.message;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 发送记录 Entity
 *
 * @author Administrator
 * @date 2020-03-07 23:20:22
 */
@Data
@TableName("t_send_record")
public class SendRecord extends BaseSend{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8981816718365688601L;

	/**
     * 通道id
     */
    @TableField("channel_id")
    private Integer channelId;

    /**
     * 手机号码
     */
    @TableField("sms_number")
    private String smsNumber;

    /**
     * 手机号码运营商
     */
    @TableField("sms_number_operator")
    private String smsNumberOperator;

    /**
     * 手机号码归属省份
     */
    @TableField("sms_number_province")
    private String smsNumberProvince;

    /**
     * 源号码
     */
    @TableField("source_number")
    private String sourceNumber;
    
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
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 2020-12-01 添加
         *  请求上游时间
     */
    @TableField("submit_time")
    private Date submitTime;
}
