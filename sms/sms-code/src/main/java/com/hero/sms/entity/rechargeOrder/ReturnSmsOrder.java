package com.hero.sms.entity.rechargeOrder;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.hero.sms.converters.AgentNameWriteConverter;
import com.hero.sms.converters.AmountWriteConverter;
import com.hero.sms.converters.OrgNameWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

/**
 * 退还短信条数记录 Entity
 *
 * @author Administrator
 * @date 2020-04-20 00:13:55
 */
@Data
@Excel("短信退还订单")
@TableName("t_return_sms_order")
public class ReturnSmsOrder {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单号
     */
    @TableField("order_no")
    @ExcelField(value = "订单号")
    private String orderNo;

    /**
     * 批次号
     */
    @TableField("send_code")
    @ExcelField(value = "批次号")
    private String sendCode;

    /**
     * 短信条数
     */
    @TableField("sms_num")
    @ExcelField(value = "短信条数")
    private Integer smsNum;

    /**
     * 商户编号
     */
    @TableField("org_code")
    @ExcelField(value = "商户",writeConverter = OrgNameWriteConverter.class)
    private String orgCode;

    /**
     * 代理id
     */
    @TableField("agent_id")
    @ExcelField(value = "代理",writeConverter = AgentNameWriteConverter.class)
    private Integer agentId;

    /**
     * 上级代理id
     */
    @TableField("up_agent_id")
    @ExcelField(value = "上级代理",writeConverter = AgentNameWriteConverter.class)
    private Integer upAgentId;

    /**
     * 商户退还金额
     */
    @TableField("org_return_amount")
    @ExcelField(value = "商户退还",writeConverter = AmountWriteConverter.class)
    private Integer orgReturnAmount;

    /**
     * 代理退还金额
     */
    @TableField("agent_return_amount")
    @ExcelField(value = "代理退还",writeConverter = AmountWriteConverter.class)
    private Integer agentReturnAmount;

    /**
     * 上级代理退还金额
     */
    @TableField("up_agent_return_amount")
    @ExcelField(value = "上级代理退还",writeConverter = AmountWriteConverter.class)
    private Integer upAgentReturnAmount;

    /**
     * 备注
     */
    @TableField("remark")
    @ExcelField(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
