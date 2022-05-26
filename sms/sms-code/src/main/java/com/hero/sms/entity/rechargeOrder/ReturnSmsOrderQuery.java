package com.hero.sms.entity.rechargeOrder;


import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ReturnSmsOrderQuery extends ReturnSmsOrder {

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

    //代理ID或上级代理ID
    private Integer agentIdQueryOr;
}
