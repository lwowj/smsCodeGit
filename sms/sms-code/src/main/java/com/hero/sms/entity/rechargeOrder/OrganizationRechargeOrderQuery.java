package com.hero.sms.entity.rechargeOrder;

import lombok.Data;

@Data
public class OrganizationRechargeOrderQuery extends OrganizationRechargeOrder {
    /**
     * 创建起始时间
     */
    //@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String orgReqStartTime;

    /**
     * 创建结束时间
     */
    //@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String orgReqEndTime;

    private Integer businessUserId;
    
    /**
     * 代理id
     */
    private String agentId;

}
