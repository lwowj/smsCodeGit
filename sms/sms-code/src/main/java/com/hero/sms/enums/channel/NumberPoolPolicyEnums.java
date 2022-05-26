package com.hero.sms.enums.channel;

public enum NumberPoolPolicyEnums {

    FAIL(0,"关闭"),POLLING(1,"轮询"),RANDOM(2,"随机");

    private Integer code;
    private String name;
    private NumberPoolPolicyEnums(Integer code, String name) {
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
        for (NumberPoolPolicyEnums numberPoolPolicyEnums : values()) {
            if(numberPoolPolicyEnums.getCode() == code) {
                return numberPoolPolicyEnums.getName();
            }
        }
        return null;
    }
}
