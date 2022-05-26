package com.hero.sms.entity.message.history;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SendBoxHistoryQuery extends SendBoxHistory {

    /**
     * 是否定时
     */
    private Boolean isTiming;
    
	/**
     * 是否定时（用于查询）
     */
    private Boolean isTimingFlag;

    /**
     * 小于等于定时时间
     */
    private Date leTimingTime;

    /**
     * 分拣开始时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date sortingStartTime;

    /**
     * 分拣结束时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date sortingEndTime;


    /**
     * 提交开始时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createStartTime;

    /**
     * 提交结束时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createEndTime;
    
    /**
     * 是否长短信
     * 1 不是
     * 2 是
     */
    private Integer isLongSms; 

}
