package com.hero.sms.entity.message;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class StatisticalChannelQuery extends StatisticalChannel {

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
