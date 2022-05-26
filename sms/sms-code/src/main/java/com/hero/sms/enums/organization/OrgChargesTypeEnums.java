package com.hero.sms.enums.organization;

public enum OrgChargesTypeEnums {

    SUBMIT("0","提交"),SUCCESS("1","成功");
    String code;
    String name;

    OrgChargesTypeEnums(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
