package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

/**
 * 用于管理后台导出发送回执
 */
@Excel("发送回执表")
@Data
public class AdminReturnRecordExcel {

    @ExcelField(value = "通道")
    private String channelName;
    @ExcelField(value = "商户编码")
    private String orgCode;
    @ExcelField(value = "商户名称")
    private String orgName;
    @ExcelField(value = "手机号码")
    private String smsNumber;
    @ExcelField(value = "消息类型")
    private String smsTypeName;
    @ExcelField(value = "批次号")
    private String sendCode;
    @ExcelField(value = "提交")
    private String reqInfo;
    @ExcelField(value = "接收")
    private String returnInfo;
    @ExcelField(value = "短信详情")
    private String msgInfo;
    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;;
}
