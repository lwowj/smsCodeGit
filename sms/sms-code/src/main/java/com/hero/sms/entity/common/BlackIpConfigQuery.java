package com.hero.sms.entity.common;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class BlackIpConfigQuery extends BlackIpConfig {

    /**
     * id数组
     */
	private List<Long> ids;
    
    /**
     * 状态数组
     */
    private List<String> limitProjectArray;
    
    /**
     * ip（模糊查询）
     */
    private String blackIpFuzzy;
    
    /**
     * 提交开始时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createStartDate;

    /**
     * 提交结束时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createEndDate;
    
    /**
     * 排序方式
     */
    private String orderByCreateTimeWay;
}
