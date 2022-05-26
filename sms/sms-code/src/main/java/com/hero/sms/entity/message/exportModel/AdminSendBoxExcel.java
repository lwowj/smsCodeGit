package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

/**
 * 用于管理后台发件箱导出
 */
@Excel("发件箱表")
@Data
public class AdminSendBoxExcel {
    @ExcelField(value = "批次号")
    private String sendCode;
    @ExcelField(value = "商户名称")
    private String orgName;
    @ExcelField(value = "商户编号")
    private String orgCode;
    @ExcelField(value = "审核状态")
    private String auditName;
    @ExcelField(value = "消息类型")
    private String msgTypeName;
    @ExcelField(value = "消息内容")
    private String smsContent;
    @ExcelField(value = "手机号码")
    private String smsNumbers;
    @ExcelField(value = "短信详情")
    private String smsInfo;
    @ExcelField(value = "金额详情")
    private String amountInfo;
    @ExcelField(value = "提交人")
    private String createUsername;
    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
