package com.hero.sms.entity.message.history;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
@Data
public class ReturnRecordHistoryQuery extends ReturnRecordHistory {

    /**
     * 提交起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reqCreateStartTime;

    /**
     * 提交结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reqCreateEndTime;
}
