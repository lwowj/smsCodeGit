package com.hero.sms.entity.message.exportModel;

import com.hero.sms.converters.AmountWriteConverter;
import com.hero.sms.converters.BusinessWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import java.util.Date;

@Excel("日报表")
public class StatisticalOrgcodeBusinessOnMonthExcel {

    /**
     * 商户商务ID
     */
    @ExcelField(value = "商务",writeConverter = BusinessWriteConverter.class)
    private Integer businessUserId;

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
     * 提交成功短信数
     */
    @ExcelField(value = "提交成功")
    private Long reqSuccessCount;


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
    @ExcelField(value = "统计日期", dateFormat = "yyyy-MM")
    private Date statisticalDate;
}
