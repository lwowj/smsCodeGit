package com.hero.sms.entity.message;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hero.sms.converters.AmountWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

/**
 * 按照商户统计 Entity
 *
 * @author Administrator
 * @date 2020-03-16 16:35:02
 */
@Data
@Excel("日报表")
@TableName("t_statistical_orgcode")
public class StatisticalOrgcode {

    /**
     * 商户编号
     */
    @TableField("org_code")
    private String orgCode;
    
    /**
     * 代理id
     */
    @TableField("agent_id")
    private Integer agentId;

    /**
     * 短信总数量
     */
    @ExcelField(value = "总数")
    @TableField("total_count")
    private Long totalCount;

    /**
     * 分拣失败短信数
     */
    @ExcelField(value = "分拣失败")
    @TableField("sorting_fail_count")
    private Long sortingFailCount;

    /**
     * 待发送短信数
     */
    @ExcelField(value = "待发送")
    @TableField("wait_req_count")
    private Long waitReqCount;

    /**
     * 提交成功短信数
     */
    @ExcelField(value = "提交成功")
    @TableField("req_success_count")
    private Long reqSuccessCount;

    /**
     * 提交失败短信数
     */
    @ExcelField(value = "提交失败")
    @TableField("req_fail_count")
    private Long reqFailCount;

    /**
     * 接收成功短信数
     */
    @ExcelField(value = "接收成功")
    @TableField("receipt_success_count")
    private Long receiptSuccessCount;
    
    /**
     * 接收失败短信数
     */
    @ExcelField(value = "接收失败")
    @TableField("receipt_fail_count")
    private Long receiptFailCount;

    /**
     * 消费金额
     */
    @ExcelField(value = "消费金额",writeConverter = AmountWriteConverter.class)
    @TableField("consume_amount")
    private Long consumeAmount;

    /**
     * 通道成本金额
     */
    @ExcelField(value = "通道成本",writeConverter = AmountWriteConverter.class)
    @TableField("channel_cost_amount")
    private Long channelCostAmount;

    /**
     * 代理利润金额
     */
    @ExcelField(value = "代理利润",writeConverter = AmountWriteConverter.class)
    @TableField("agent_income_amount")
    private Long agentIncomeAmount;

    /**
     * 平台利润金额
     */
    @ExcelField(value = "平台利润",writeConverter = AmountWriteConverter.class)
    @TableField("income_amount")
    private Long incomeAmount;

    /**
     * 统计日期
     */
    @ExcelField(value = "统计日期", dateFormat = "yyyy-MM-dd")
    @TableField("statistical_date")
    private Date statisticalDate;

}
