package com.hero.sms.entity.rechargeOrder;

import lombok.Data;

@Data
public class ReturnSmsOrderExt extends ReturnSmsOrder {

    private String orgName;
    private String agentName;
    private String upAgentName;
}
