package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.hero.sms.converters.OrgNameWriteConverter;
import com.hero.sms.converters.SendRecordStateWriterConverter;
import com.hero.sms.converters.SmsTypeWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

@Excel("发送记录信息表")
@Data
public class SendRecordExtExcel {


    @ExcelField(value = "批次号")
    private String sendCode;

    @ExcelField(value = "商户名称",writeConverter = OrgNameWriteConverter.class)
    private String orgCode;

    @ExcelField(value = "手机号码")
    private String smsNumber;

    @ExcelField(value = "消息内容")
    private String smsContent;

    @ExcelField(value = "短信详情")
    private String smsInfo;

    @ExcelField(value = "运营商详情")
    private String operatorInfo;

    @ExcelField(value = "状态",writeConverter = SendRecordStateWriterConverter.class)
    private Integer state;

    @ExcelField(value = "消息类型",writeConverter = SmsTypeWriteConverter.class)
    private Integer smsType;

    @ExcelField(value = "回执耗时")
    private String returnTime;

    @ExcelField(value = "回执时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date receiptTime;

    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
