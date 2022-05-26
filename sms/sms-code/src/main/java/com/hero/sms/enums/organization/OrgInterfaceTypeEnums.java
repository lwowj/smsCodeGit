package com.hero.sms.enums.organization;

import com.hero.sms.enums.BaseEnum;

public enum OrgInterfaceTypeEnums implements BaseEnum {

    Http(1,"HTTP"),Smpp(2,"SMPP");
    Integer code;
    String name;

    OrgInterfaceTypeEnums(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
