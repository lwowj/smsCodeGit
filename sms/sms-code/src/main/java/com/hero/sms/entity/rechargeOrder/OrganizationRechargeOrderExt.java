package com.hero.sms.entity.rechargeOrder;

import lombok.Data;

@Data
public class OrganizationRechargeOrderExt extends OrganizationRechargeOrder {

    private Integer businessUserId;

    private String orgName;
    
    private Integer agentId;
    
    private String agentName;

}
