package com.hero.sms.entity.message;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ReceiptReturnRecordAbnormalQuery extends ReceiptReturnRecordAbnormal {

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
    
    /**
     * 通道ID是否可为空
     */
    private Boolean isChannelIdFlag;
    
    /**
     * 排序方式
     */
    private String orderByCreateTimeWay;
    
    /**
     * 状态数组
     */
    private Integer[] stateArray;
    
    private List<Long> ids;
}
