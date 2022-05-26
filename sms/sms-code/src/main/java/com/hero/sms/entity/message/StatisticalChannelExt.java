package com.hero.sms.entity.message;

import lombok.Data;

@Data
public class StatisticalChannelExt extends StatisticalChannel {

    private String channelName;

    private Long realReqSuccessCount;

}
