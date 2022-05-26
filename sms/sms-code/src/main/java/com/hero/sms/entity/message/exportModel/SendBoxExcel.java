package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.hero.sms.converters.AuditStateWriteConverter;
import com.hero.sms.converters.SmsNumbersWriteConverter;
import com.hero.sms.converters.SmsTypeWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

@Excel("发件箱信息表")
@Data
public class SendBoxExcel {

    @ExcelField(value = "批次号")
    private String sendCode;

    @ExcelField(value = "消息类型",writeConverter = SmsTypeWriteConverter.class)
    private Integer smsType;

    @ExcelField(value = "消息内容")
    private String smsContent;

    @ExcelField(value = "审核状态",writeConverter = AuditStateWriteConverter.class)
    private Integer auditState;

    @ExcelField(value = "手机号码",writeConverter = SmsNumbersWriteConverter.class)
    private String smsNumbers;

    @ExcelField(value = "信息详情")
    private String smsInfo;

    @ExcelField(value = "提交账号")
    private String createUsername;

    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
