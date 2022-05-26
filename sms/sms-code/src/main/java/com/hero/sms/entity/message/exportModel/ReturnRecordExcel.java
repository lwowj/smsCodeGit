package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.hero.sms.converters.CommonStateWriteConverter;
import com.hero.sms.converters.SmsTypeWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

@Data
@Excel("回执信息表")
public class ReturnRecordExcel {

    @ExcelField(value = "批次号")
    private String sendCode;

    @ExcelField(value = "手机号码")
    private String smsNumber;

    @ExcelField(value = "提交")
    private String reqInfo;

    @ExcelField(value = "接收状态",writeConverter = CommonStateWriteConverter.class)
    private Integer returnState;

    @ExcelField(value = "消息类型",writeConverter = SmsTypeWriteConverter.class)
    private Integer smsType;

    @ExcelField(value = "信息详情")
    private String smsInfo;

    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
