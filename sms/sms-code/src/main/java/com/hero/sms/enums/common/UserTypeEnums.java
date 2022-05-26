package com.hero.sms.enums.common;



public enum UserTypeEnums {

    Admin(0,"管理用户"),Organization(1,"商户用户"),Agent(2,"代理用户");

    private Integer code;
    private String name;
    private UserTypeEnums(Integer code, String name) {
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
        for (UserTypeEnums userTypeEnums : values()) {
            if(userTypeEnums.getCode().intValue() == code.intValue()) {
                return userTypeEnums.getName();
            }
        }
        return null;
    }
}
