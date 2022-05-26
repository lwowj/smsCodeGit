package com.hero.sms.entity.msg;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 站内信 Entity
 *
 * @author Administrator
 * @date 2020-08-21 20:57:43
 */
@Data
@TableName("t_message")
public class Message {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 消息类型(投诉、建议、其它)
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息内容
     */
    @TableField("message_contact")
    private String messageContact;

    /**
     * 图片地址
     */
    @TableField("picture_url")
    private String pictureUrl;

    /**
     * 管理端阅读状态
     */
    @TableField("admin_read_status")
    private String adminReadStatus;

    /**
     * 商户端阅读状态
     */
    @TableField("merch_read_status")
    private String merchReadStatus;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建用户
     */
    @TableField("create_User")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField("create_Date")
    private Date createDate;

    /**
     * 图片文件
     */
    @TableField(exist = false)
    private MultipartFile pictureFile;

}
