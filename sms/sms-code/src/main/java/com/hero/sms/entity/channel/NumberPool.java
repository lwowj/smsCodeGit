package com.hero.sms.entity.channel;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 号码池 Entity
 *
 * @author Administrator
 * @date 2020-04-15 21:08:21
 */
@Data
@TableName("t_number_pool")
public class NumberPool {

    /**
     * 号码池ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 手机号码
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 池组ID
     */
    @TableField("group_id")
    private Integer groupId;
    /**
     * 状态
     */
    @TableField("state")
    private Integer state;

}
