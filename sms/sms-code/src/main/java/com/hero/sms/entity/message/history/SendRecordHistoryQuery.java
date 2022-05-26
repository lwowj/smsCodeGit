package com.hero.sms.entity.message.history;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SendRecordHistoryQuery extends SendRecordHistory {
    /**
     * 创建起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createStartTime;

    /**
     * 创建结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createEndTime;
    
    /**
     * 提交起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitStartTime;

    /**
     * 提交结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitEndTime;
}
