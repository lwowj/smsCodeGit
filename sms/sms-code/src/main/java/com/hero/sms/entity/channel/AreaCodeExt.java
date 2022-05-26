package com.hero.sms.entity.channel;

import lombok.Data;

@Data
public class AreaCodeExt extends AreaCode{


    /**
     * 我方标识归属地区
     */
    private String inAreaStr;

    /**
     * 对外标识归属地区
     */
    private String outAreaStr;

    /**
     * 归属地区编码
     */
    private String areaCodingStr;

    /**
     * 归属地区名称
     */
    private String areaNameStr;
}
