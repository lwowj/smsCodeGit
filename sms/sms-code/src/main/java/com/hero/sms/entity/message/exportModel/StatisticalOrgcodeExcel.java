package com.hero.sms.entity.message.exportModel;

import java.util.Date;

import com.hero.sms.converters.AmountWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import lombok.Data;

@Data
@Excel("日报表")
public class StatisticalOrgcodeExcel {

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

/*    *//**
     * 待发送短信数
     *//*
    @ExcelField(value = "待发送")
    private Long waitReqCount;*/

    /**
     * 提交成功短信数
     */
    @ExcelField(value = "提交成功")
    private Long reqSuccessCount;

/*
    */
/**
     * 提交失败短信数
     *//*

    @ExcelField(value = "提交失败")
    private Long reqFailCount;
*/

    @ExcelField(value = "消费金额",writeConverter = AmountWriteConverter.class)
    private Long consumeAmount;

    /**
     * 统计日期
     */
    @ExcelField(value = "统计日期", dateFormat = "yyyy-MM-dd")
    private Date statisticalDate;
}
