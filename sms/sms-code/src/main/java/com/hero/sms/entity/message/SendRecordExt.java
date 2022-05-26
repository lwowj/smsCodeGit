package com.hero.sms.entity.message;

import com.hero.sms.common.utils.DateUtil;

import lombok.Data;

/**
 * 短信记录扩展
 */
@Data
public class SendRecordExt extends SendRecord {

    private String smsChannelName;

    private String orgName;

    /**
     * 短信详情
     */
    private String smsInfo;

    /**
     * 金额详情
     */
    private String amountInfo;

    /**
     * 运营商详情
     */
    private String operatorInfo;

    /**
     * 发送和接收的时间差
     */
    private String returnTime;

    public String getReturnTime() {
        String result = null;
        if (this.getReceiptTime() == null || this.getCreateTime() == null){
            return result;
        }
        return DateUtil.getDistanceTimes(this.getCreateTime(),this.getReceiptTime());
    }

    /**
     * 创建记录和提交发送的时间差
     */
    private String elapsedTime;

    public String getElapsedTime() {
        String result = null;
        if (this.getSubmitTime() == null || this.getCreateTime() == null){
            return result;
        }
        return DateUtil.getDistanceTimes(this.getCreateTime(),this.getSubmitTime());
    }
}
