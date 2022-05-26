package com.hero.sms.entity.message.history;

import com.hero.sms.common.utils.DateUtil;

import lombok.Data;

@Data
public class ReturnRecordHistoryExt extends ReturnRecordHistory{

    private String smsChannelName;

    private String orgName;

    private String msgInfo;

    private String reqInfo;

    private String returnInfo;

    private String smsTypeName;

    /**
     * 发送和接收的时间差
     */
    private String returnTime;

    public String getReturnTime() {
        String result = null;
        if (this.getCreateTime() == null || this.getReqCreateTime() == null){
            return result;
        }
        return DateUtil.getDistanceTimes(this.getReqCreateTime(),this.getCreateTime());
    }
}
