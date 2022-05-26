package com.hero.sms.entity.message;

import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 接收回执信息异常表 Entity
 *
 * @author Administrator
 * @date 2020-11-21 17:50:39
 */
@Data
@TableName("t_receipt_return_record_abnormal")
public class ReceiptReturnRecordAbnormal {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * json参数
     */
    @TableField("return_dataparam")
    private String returnDataparam;

    /**
     * 处理状态
     */
    @TableField("processing_state")
    private Integer processingState;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 重推时间
     */
    @TableField("again_time")
    private Date againTime;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

}
