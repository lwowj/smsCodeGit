package com.hero.sms.entity.msg;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 站内信回复 Entity
 *
 * @author Administrator
 * @date 2020-08-16 17:26:11
 */
@Data
@TableName("t_message_reply")
public class MessageReply {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息id
     */
    @TableField("message_id")
    private Integer messageId;

    /**
     * 回复内容
     */
    @TableField("reply_contact")
    private String replyContact;

    /**
     * 图片地址
     */
    @TableField("picture_url")
    private String pictureUrl;

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
