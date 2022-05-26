package com.hero.sms.enums.common;

import com.hero.sms.enums.BaseEnum;

public enum SwitchEnums implements BaseEnum {

    SUCCESS(1,"开启"),FAIL(0,"关闭");

    private Integer code;
    private String name;
    private SwitchEnums(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        if (code == null) return null;
        for (SwitchEnums switchEnums : values()) {
            if(switchEnums.getCode() == code) {
                return switchEnums.getName();
            }
        }
        return null;
    }
}
