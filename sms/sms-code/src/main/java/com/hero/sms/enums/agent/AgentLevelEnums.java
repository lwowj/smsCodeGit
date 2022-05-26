package com.hero.sms.enums.agent;

import com.hero.sms.enums.BaseEnum;

public enum AgentLevelEnums implements BaseEnum {

    LEVEL_1(1,"一级代理"),LEVEL_2(2,"二级代理");

    private int code;
    private String name;

    private AgentLevelEnums(int code, String name){
        this.code = code;
        this.name = name;
    }


    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
