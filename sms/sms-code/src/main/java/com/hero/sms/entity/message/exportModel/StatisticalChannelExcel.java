package com.hero.sms.entity.message.exportModel;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hero.sms.converters.AmountWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

@Data
@Excel("通道日报表")
public class StatisticalChannelExcel {

    @ExcelField(value = "通道名称")
    private String channelName;

    /**
     * 短信总数量
     */
    @ExcelField(value = "总数")
    private Long totalCount;

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

    @ExcelField(value = "成功率")
    private String successRate;

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
    @ExcelField(value = "统计日期", dateFormat = "yyyy-MM-dd")
    private Date statisticalDate;

    public String getSuccessRate() {
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);
        return percent.format(this.receiptSuccessCount.doubleValue()/this.totalCount.doubleValue());
    }

    public Long getReqSuccessCount() {
        return this.reqSuccessCount + this.receiptSuccessCount + this.receiptFailCount;
    }
}
