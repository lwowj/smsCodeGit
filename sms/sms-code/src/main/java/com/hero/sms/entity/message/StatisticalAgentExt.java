package com.hero.sms.entity.message;

import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

@Data
public class StatisticalAgentExt extends StatisticalAgent {

    /**
     * 成功率
     */
    @ExcelField(value = "成功率")
    private String rateSuccess;
}
