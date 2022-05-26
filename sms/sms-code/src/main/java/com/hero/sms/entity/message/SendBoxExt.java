package com.hero.sms.entity.message;

import com.hero.sms.common.utils.DateUtil;

import lombok.Data;

@Data
public class SendBoxExt extends SendBox {

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
     * 创建与分拣时间差
     */
    private String returnTime;

    public String getReturnTime() {
        String result = null;
        if (this.getSortingTime() == null || this.getCreateTime() == null){
            return result;
        }
        return DateUtil.getDistanceTimes(this.getCreateTime(),this.getSortingTime());
    }
}
