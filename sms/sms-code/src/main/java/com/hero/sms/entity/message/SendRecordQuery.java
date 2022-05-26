package com.hero.sms.entity.message;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SendRecordQuery extends SendRecord {

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
     * 前置自定义语句
     */
    private String beforeApplyString;
    
    public Integer subTypeWith;

    /**
     * 分页查询是否统计total
     */
    private Boolean searchCount;

    private Integer submitSuccessCount;
	
    private Integer groupId;
    
    /**
     * 通道ID是否可为空
     */
    private Boolean isChannelIdFlag;
    
    /**
     * 消费金额是否可为空
     */
    private Boolean isConsumeAmountFlag;
    
    /**
     * 消费金额不等于某值
     */
    private Integer notIsConsumeAmount;
    
    /**
     * 排序方式
     */
    private String orderByCreateTimeWay;
    
    /**
     * 状态数组
     */
    private Integer[] stateArray;
    
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
    
    /**
     * 上游resid是否为空
     */
    private Boolean resMsgidIsNullFlag;
}
