package com.hero.sms.entity.channel;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 号码池组表 Entity
 *
 * @author Administrator
 * @date 2020-04-15 21:09:40
 */
@Data
@TableName("t_number_pool_group")
public class NumberPoolGroup {

    /**
     * 号码池组id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

}
