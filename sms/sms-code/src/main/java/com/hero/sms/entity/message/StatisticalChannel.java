package com.hero.sms.entity.message;

import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 按照通道统计 Entity
 *
 * @author Administrator
 * @date 2020-05-28 15:59:07
 */
@Data
@TableName("t_statistical_channel")
public class StatisticalChannel {

    /**
     * 通道id
     */
    @TableField("channel_id")
    private Integer channelId;

    /**
     * 短信总数量
     */
    @TableField("total_count")
    private Long totalCount;

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
    @TableField("receipt_success_count")
    private Long receiptSuccessCount;

    /**
     * 接收失败数
     */
    @TableField("receipt_fail_count")
    private Long receiptFailCount;

    /**
     * 通道成本金额
     */
    @TableField("channel_cost_amount")
    private Long channelCostAmount;

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
