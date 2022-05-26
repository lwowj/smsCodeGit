package com.hero.sms.entity.message.exportModel;

import com.hero.sms.converters.AgentNameWriteConverter;
import com.hero.sms.converters.AmountWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import java.util.Date;

@Excel("日报表")
public class StatisticalAgentExcel {

    /**
     * 代理id
     */
    @ExcelField(value = "代理",writeConverter = AgentNameWriteConverter.class)
    private Integer agentId;

    /**
     * 短信总数量
     */
    @ExcelField(value = "总数")
    private Long totalCount;

    /**
     * 分拣失败短信数
     */
    @ExcelField(value = "分拣失败")
    private Long sortingFailCount;

    /**
     * 待发送短信数
     */
    @ExcelField(value = "待发送")
    private Long waitReqCount;

    /**
     * 提交成功短信数
     */
    @ExcelField(value = "提交成功")
    private Long reqSuccessCount;

    /**
     * 提交失败短信数
     */
    @ExcelField(value = "提交失败")
    private Long reqFailCount;

    /**
     * 接收成功短信数
     */
    @ExcelField(value = "接收成功")
    private Long receiptSuccessCount;

    /**
     * 接收失败短信数
     */
    @ExcelField(value = "接收失败")
    private Long receiptFailCount;

    /**
     * 成功率
     */
    @ExcelField(value = "成功率")
    private String rateSuccess;

    /**
     * 消费金额
     */
    @ExcelField(value = "消费金额",writeConverter = AmountWriteConverter.class)
    private Long consumeAmount;

    /**
     * 代理利润金额
     */
    @ExcelField(value = "代理利润",writeConverter = AmountWriteConverter.class)
    private Long agentIncomeAmount;

    /**
     * 通道成本金额
     */
    @ExcelField(value = "通道成本",writeConverter = AmountWriteConverter.class)
    private Long channelCostAmount;


    /**
     * 平台利润金额
     */
    @ExcelField(value = "利润",writeConverter = AmountWriteConverter.class)
    private Long incomeAmount;

    /**
     * 统计日期
     */
    @ExcelField(value = "日期", dateFormat = "yyyy-MM-dd")
    private Date statisticalDate;
}
