package com.hero.sms.entity.channel;

import lombok.Data;

@Data
public class GatewayConnectInfo {

    private String Id;
    private String host;
    private Integer port;
    private String localhost;
    private Integer localport;
    private short maxChannels;
    private boolean isReSendFailMsg;
    private short maxRetryCnt;
    private short retryWaitTimeSec;
    private short idleTimeSec;
    boolean closeWhenRetryFailed;
    private int readLimit;
    private int writeLimit;
    private boolean useSSL;
    private String proxy;
    private int connectionNum;
}
