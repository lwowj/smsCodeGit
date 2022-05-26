package com.hero.sms.entity.channel;

import lombok.Data;

@Data
public class SmsChannelQuery extends SmsChannel {
    private String groupId;
    private String supportArea;
    private String channelIp;
}
