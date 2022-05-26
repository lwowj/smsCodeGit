package com.hero.sms.entity.agentRemitOrder;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代理提现订单 Entity
 *
 * @author Administrator
 * @date 2020-04-02 22:24:19
 */
@Data
@TableName("t_agent_remit_order")
public class AgentRemitOrder {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代理id
     */
    @TableField("agent_id")
    private String agentId;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 银行编码
     */
    @TableField("bank_code")
    private String bankCode;

    /**
     * 姓名
     */
    @TableField("bank_account_name")
    private String bankAccountName;

    /**
     * 银行卡号
     */
    @TableField("bank_account_no")
    private String bankAccountNo;

    /**
     * 支行名称
     */
    @TableField("bank_branch")
    private String bankBranch;

    /**
     * 转账凭证
     */
    @TableField("remit_picture_url")
    private String remitPictureUrl;

    /**
     * 提现金额(分)
     */
    @TableField("remit_amount")
    private Integer remitAmount;

    /**
     * 可用利润(分)
     */
    @TableField("available_amount")
    private Integer availableAmount;

    /**
     * 审核状态
     */
    @TableField("status")
    private String status;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private Date approveTime;

    /**
     * 防篡改
     */
    @TableField("data_md5")
    private String dataMd5;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField("ceate_user")
    private String ceateUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;


    /**
     * 转账凭证文件
     */
    @TableField(exist = false)
    private MultipartFile remitPictureFile;

}
