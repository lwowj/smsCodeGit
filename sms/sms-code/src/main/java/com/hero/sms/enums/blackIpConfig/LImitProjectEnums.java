package com.hero.sms.enums.blackIpConfig;


/**
 * 项目范围
 * @author payBoo
 *
 */
public enum LImitProjectEnums {

    All("ALL","所有"),Admin("ZH","总后台"),Organization("MERCH","商户"),Agent("AGENT","代理"),GateWay("GATEWAY","网关");

    private String code;
    private String name;
    private LImitProjectEnums(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        if (code == null) return null;
        for (LImitProjectEnums limitProjectEnums : values()) {
            if((limitProjectEnums.getCode()).equals(code)) {
                return limitProjectEnums.getName();
            }
        }
        return null;
    }
}
