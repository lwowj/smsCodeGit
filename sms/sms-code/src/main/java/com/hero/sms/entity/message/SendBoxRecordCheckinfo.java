package com.hero.sms.entity.message;


import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 发件箱分拣发送记录提交中间校验表 Entity
 *
 * @author Administrator
 * @date 2021-01-13 18:01:35
 */
@Data
@TableName("t_send_box_record_checkinfo")
public class SendBoxRecordCheckinfo {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 批次号
     */
    @TableField("send_code")
    private String sendCode;

    /**
     * 手机号码
     */
    @TableField("sms_number")
    private String smsNumber;

    /**
     * 状态：0开锁；1锁定。默认1
     */
    @TableField("state")
    private Integer state;

    /**
     * 
     */
    @TableField("create_time")
    private Date createTime;

}
