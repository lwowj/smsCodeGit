package com.hero.sms.enums.organization;

public enum OrgChargesTypeEnums {

    SUBMIT("0","ζδΊ€"),SUCCESS("1","ζε");
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
