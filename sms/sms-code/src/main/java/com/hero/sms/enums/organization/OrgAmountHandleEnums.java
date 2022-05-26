package com.hero.sms.enums.organization;

public enum OrgAmountHandleEnums {

    Undo("0","不处理"),ReturnAgent("1","转入代理");
    String code;
    String name;

    OrgAmountHandleEnums(String code, String name){
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
