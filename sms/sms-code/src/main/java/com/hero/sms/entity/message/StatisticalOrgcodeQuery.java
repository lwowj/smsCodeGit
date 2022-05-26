package com.hero.sms.entity.message;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class StatisticalOrgcodeQuery extends StatisticalOrgcode {

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

    /**
     * 统计起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date statisticalStartMonth;

    /**
     * 统计结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date statisticalEndMonth;

    /**
     * 商户商务ID
     */
    private Integer businessUserId;
}
