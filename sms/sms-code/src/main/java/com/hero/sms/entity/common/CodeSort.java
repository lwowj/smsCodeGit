package com.hero.sms.entity.common;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代码分类 Entity
 *
 * @author MrJac
 * @date 2020-03-04 16:53:27
 */
@Data
@TableName("code_sort")
public class CodeSort {

    /**
     * 主键自增
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 分类名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 
     */
    @TableField("CODE")
    private String code;

    /**
     * 上级代码
     */
    @TableField("PARENT_CODE")
    private String parentCode;

    /**
     * 描述
     */
    @TableField("DESCREPTION")
    private String descreption;

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
