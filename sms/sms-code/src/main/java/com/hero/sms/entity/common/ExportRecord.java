package com.hero.sms.entity.common;

import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 导出文件记录表 Entity
 *
 * @author Administrator
 * @date 2020-05-26 11:31:36
 */
@Data
@TableName("t_export_record")
public class ExportRecord {

    /**
     * id

     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 用户类型
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 文件类型
     */
    @TableField("type")
    private String type;

    /**
     * 文件名称
     */
    @TableField("filename")
    private String filename;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

}
