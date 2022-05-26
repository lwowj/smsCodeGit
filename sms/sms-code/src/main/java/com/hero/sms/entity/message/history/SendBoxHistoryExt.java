package com.hero.sms.entity.message.history;

import lombok.Data;

@Data
public class SendBoxHistoryExt extends SendBoxHistory{

    private String orgName;

    /**
     * 短信详情
     */
    private String smsInfo;

    /**
     * 金额详情
     */
    private String amountInfo;

    private String auditName;

    private String msgTypeName;

}
