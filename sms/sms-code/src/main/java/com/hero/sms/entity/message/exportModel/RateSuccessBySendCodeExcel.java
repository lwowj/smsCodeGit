package com.hero.sms.entity.message.exportModel;

import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

import java.text.NumberFormat;
import java.util.Date;

@Data
@Excel("批次成功率")
public class RateSuccessBySendCodeExcel {

    @ExcelField(value = "批次号")
    private String sendCode;

    @ExcelField(value = "代理")
    private String agentName;

    @ExcelField(value = "商户名称")
    private String orgName;

    @ExcelField(value = "消息内容")
    private String smsContent;
    /**
     * 提交成功短信数
     */
    @ExcelField(value = "提交成功")
    private Long total;

    /**
     * 提交成功短信数
     */
    @ExcelField(value = "接收成功")
    private Long receiptSuccess;

    @ExcelField(value = "接收失败")
    private Long receiptFail;

    @ExcelField(value = "成功率")
    private String rateSuccess;

    public String getRateSuccess() {
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);
        return percent.format(this.receiptSuccess.doubleValue()/this.total.doubleValue());
    }

    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
