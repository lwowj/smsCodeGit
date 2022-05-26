package com.hero.sms.entity.message;

import com.hero.sms.converters.AmountWriteConverter;
import com.hero.sms.converters.BusinessWriteConverter;
import com.hero.sms.converters.OrgNameWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

@Excel("日报表")
@Data
public class StatisticalOrgcodeExt extends StatisticalOrgcode{

    /**
     * 商户商务ID
     */
    @ExcelField(value = "商务",writeConverter = BusinessWriteConverter.class)
    private Integer businessUserId;

    @ExcelField(value = "商户名称",writeConverter = OrgNameWriteConverter.class)
    private String orgCode;

    private String orgName;

    private Long realReqSuccessCount;
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
     * 通道成本金额
     */
    @ExcelField(value = "通道成本",writeConverter = AmountWriteConverter.class)
    private Long channelCostAmount;

    /**
     * 代理利润金额
     */
    @ExcelField(value = "代理利润",writeConverter = AmountWriteConverter.class)
    private Long agentIncomeAmount;

    /**
     * 平台利润金额
     */
    @ExcelField(value = "平台利润",writeConverter = AmountWriteConverter.class)
    private Long incomeAmount;
    @ExcelField(value = "短信退还数")
    private Long smsNum;
    private Long orgReturnAmount;
    private String costName;
    @ExcelField(value = "短信资费",writeConverter = AmountWriteConverter.class)
    private String costValue;

    /**
     * 统计日期
     */
    @ExcelField(value = "统计日期", dateFormat = "yyyy-MM-dd")
    private Date statisticalDate;
}
