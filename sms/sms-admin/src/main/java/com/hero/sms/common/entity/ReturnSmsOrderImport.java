package com.hero.sms.common.entity;

import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

@Data
@Excel("短信退还导入数据")
public class ReturnSmsOrderImport {
    /**
     * 字段1
     */
    @ExcelField(value = "批次号", required = true,maxLength = 21, regularExp = "[B]{1}[N]{1}[0-9]{14}[0-9|a-z|A-Z]{5}",
            regularExpMessage = "非批次号格式",comment = "提示：必填，长度不能超过21个字符")
    private String sendCode;

    /**
     * 字段2
     */
    @ExcelField(value = "退还条数", required = true, maxLength = 5, regularExp = "[0-9]+",
            regularExpMessage = "必须是数字", comment = "提示: 必填，只能填写数字，并且长度不能超过5位")
    private Integer smsNum;

    /**
     * 字段3
     */
    @ExcelField(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;
}
