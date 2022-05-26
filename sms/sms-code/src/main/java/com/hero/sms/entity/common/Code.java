package com.hero.sms.entity.common;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代码表 Entity
 *
 * @author MrJac
 * @date 2020-03-04 21:15:50
 */
@Data
@TableName("code")
public class Code {

    /**
     * 主键自增
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 分类代码
     */
    @TableField("SORT_CODE")
    private String sortCode;

    /**
     * 代码
     */
    @TableField("CODE")
    private String code;

    /**
     * 代码名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 上级代码
     */
    @TableField("PARENT_CODE")
    private String parentCode;

    /**
     * 排序
     */
    @TableField("ORDER_LEVEL")
    private Integer orderLevel;

    /**
     * 是否删除（1是 0否）
     */
    @TableField("IS_DELETE")
    private String isDelete;

    /**
     * 描述
     */
    @TableField("DESCRIPTION")
    private String description;

    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;

}
