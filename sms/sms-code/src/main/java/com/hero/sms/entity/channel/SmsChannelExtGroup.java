package com.hero.sms.entity.channel;

import lombok.Data;

@Data
public class SmsChannelExtGroup extends SmsChannel{

    private String groupIds;
    private String groupNames;
    private Integer daySendNum;
    private String areaCodes;
    private String areaNames;
    private String propertyValue;
    private String propertyName;
}
