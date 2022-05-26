package com.hero.sms.entity.message.exportModel;

import com.hero.sms.converters.AmountWriteConverter;
import com.hero.sms.converters.OrgNameWriteConverter;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

@Data
@Excel("商户发送量排行")
public class StatisticalOrgTotalExcel {

    @ExcelField(value = "商户名称",writeConverter = OrgNameWriteConverter.class)
    private String orgCode;

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
    private Long realReqSuccessCount;

    /**
     * 消费金额
     */
    @ExcelField(value = "消费金额",writeConverter = AmountWriteConverter.class)
    private Long consumeAmount;

    /**
     * 代理利润金额
     */
    @ExcelField(value = "代理利润",writeConverter = AmountWriteConverter.class)
    private Long agentIncomeAmount;

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



}
