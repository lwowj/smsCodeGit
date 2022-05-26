package com.hero.sms.entity.message;

import java.util.List;

import lombok.Data;

@Data
public class SendRecordCheckinfoPlanQuery extends SendRecordCheckinfoPlan {

    private List<Long> idsList;
    
    private String states;
}
