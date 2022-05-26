package com.hero.sms.entity.message;

import lombok.Data;

@Data
public class ReceiptReturnRecordAbnormalExt extends ReceiptReturnRecordAbnormal {

    private String smsChannelName;

    private String msgInfo;

    private String reqInfo;

    private String returnInfo;
    
    private String processingStateInfo;
}
