package com.hero.sms.entity.message;

import java.util.List;

import lombok.Data;

@Data
public class SendRecordCheckinfoQuery extends SendRecordCheckinfo {

    private List<Long> idsList;
    
    private String states;
}
