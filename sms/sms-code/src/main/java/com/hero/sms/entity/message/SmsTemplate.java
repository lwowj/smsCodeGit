package com.hero.sms.entity.message;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 短信模板表 Entity
 *
 * @author Administrator
 * @date 2020-03-11 20:08:30
 */
@Data
@TableName("t_sms_template")
public class SmsTemplate {

    /**
     * id
模板ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户编号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 商户名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 模板名称
     */
    @TableField("template_Name")
    private String templateName;

    /**
     * 模板内容
     */
    @TableField("template_Content")
    private String templateContent;

    /**
     * 提交人名称
     */
    @TableField("submitter_name")
    private String submitterName;

    /**
     * 提交人id
     */
    @TableField("submitter_id")
    private Integer submitterId;

    /**
     * 审核状态(0待审核 1通过 2拒绝)
     */
    @TableField("approve_Status")
    private Integer approveStatus;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建日期
     */
    @TableField("create_date")
    private Date createDate;

}
