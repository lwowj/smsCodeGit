package com.hero.sms.entity.message;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

/**
 * 按照代理统计 Entity
 *
 * @author Administrator
 * @date 2020-03-20 14:55:46
 */
@Data
@TableName("t_statistical_agent")
public class StatisticalAgent {

    /**
     * 代理id
     */
    @TableField("agent_id")
    private Integer agentId;

    /**
     * 短信总数量
     */
    @TableField("total_count")
    private Long totalCount;

    /**
     * 分拣失败短信数
     */
    @TableField("sorting_fail_count")
    private Long sortingFailCount;

    /**
     * 待发送短信数
     */
    @TableField("wait_req_count")
    private Long waitReqCount;

    /**
     * 提交成功短信数
     */
    @TableField("req_success_count")
    private Long reqSuccessCount;

    /**
     * 提交失败短信数
     */
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
    @TableField("consume_amount")
    private Long consumeAmount;

    /**
     * 通道成本金额
     */
    @TableField("channel_cost_amount")
    private Long channelCostAmount;

    /**
     * 代理利润金额
     */
    @TableField("agent_income_amount")
    private Long agentIncomeAmount;

    /**
     * 平台利润金额
     */
    @TableField("income_amount")
    private Long incomeAmount;

    /**
     * 统计日期
     */
    @TableField("statistical_date")
    private Date statisticalDate;

}
