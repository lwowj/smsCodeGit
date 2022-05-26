package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

/**
 * 用于管理后台导出发送记录
 */
@Excel("发送记录表")
@Data
public class AdminSendRecordExcel {

    @ExcelField(value = "通道")
    private String channelName;
    @ExcelField(value = "商户编号")
    private String orgCode;
    @ExcelField(value = "商户名称")
    private String orgName;
    @ExcelField(value = "手机号码")
    private String smsNumber;
    @ExcelField(value = "消息内容")
    private String smsContent;
    @ExcelField(value = "短信详情")
    private String smsInfo;
    @ExcelField(value = "金额详情")
    private String amountInfo;
    @ExcelField(value = "运营商")
    private String operatorInfo;
    @ExcelField(value = "短信类型")
    private String smsTypeName;
    @ExcelField(value = "状态")
    private String stateName;
    @ExcelField(value = "回执耗时")
    private String returnTime;
    @ExcelField(value = "回执时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date receiptTime;
    @ExcelField(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
