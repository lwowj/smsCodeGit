package com.hero.sms.entity.channel;

import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 通道分组表 Entity
 *
 * @author Administrator
 * @date 2020-06-20 22:38:31
 */
@Data
@TableName("t_sms_channel_group")
public class SmsChannelGroup {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户分组ID
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 通道id
     */
    @TableField("sms_channel_id")
    private Integer smsChannelId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

}
