package com.hero.sms.entity.message;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class StatisticalAgentQuery extends StatisticalAgent {

    /**
     * 统计起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date statisticalStartTime;

    /**
     * 统计结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date statisticalEndTime;

}
