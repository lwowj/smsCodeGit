package com.hero.sms.entity.channel;


import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 国家地区表 Entity
 *
 * @author Administrator
 * @date 2022-03-18 15:41:19
 */
@Data
@TableName("t_area_code")
public class AreaCode {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 我方标识归属地区
     */
    @TableField("in_area")
    private String inArea;

    /**
     * 对外标识归属地区
     */
    @TableField("out_area")
    private String outArea;

    /**
     * 归属地区编码
     */
    @TableField("area_coding")
    private String areaCoding;

    /**
     * 归属地区名称
     */
    @TableField("area_name")
    private String areaName;


    /**
     * 排序
     */
    @TableField("order_num")
    private Long orderNum;
    
    /**
     * 扩展字段
     */
    @TableField("extend")
    private String extend;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

}
