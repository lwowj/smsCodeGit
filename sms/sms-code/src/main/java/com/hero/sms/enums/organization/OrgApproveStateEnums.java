package com.hero.sms.enums.organization;

import com.hero.sms.enums.BaseEnum;

/**
 * 认证状态
 */
public enum OrgApproveStateEnums implements BaseEnum {

    UNVERIFIED("1","未认证","volcano"),SUCCESS("2","认证通过","green"),FAIL("3","认证失败","volcano");

    private String code;
    private String name;
    private String color;
    private OrgApproveStateEnums(String code,String name, String color){
        this.code = code;
        this.name = name;
        this.color = color;
    }

    public String getCode(){
        return code;
    }
    public String getName(){
        return name;
    }
    public String getColor() {
        return color;
    }
}
