package com.hero.sms.entity.rechargeOrder;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.common.Code;
import lombok.Data;

@Data
public class StatisticBean {
    Long sumRechargeAmount;
    String netwayCode;
    String netwayName;

    public void setNetwayCode(String netwayCode) {
        this.netwayCode = netwayCode;
        Code code = DatabaseCache.getCodeBySortCodeAndCode("NetwayCode",netwayCode);
        if (code != null){
            this.netwayName = code.getName();
        }else {
            this.netwayName = netwayCode;
        }
    }
}
