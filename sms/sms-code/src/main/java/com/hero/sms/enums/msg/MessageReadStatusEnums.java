package com.hero.sms.enums.msg;

import com.hero.sms.enums.BaseEnum;

/**
 * 消息阅读状态
 */
public enum MessageReadStatusEnums implements BaseEnum {

    READ("1","已读","green"),
    UNREAD("2","未读","volcano");

    private String code;
    private String name;
    private String color;
    MessageReadStatusEnums(String code, String name, String color) {
        this.code = code;
        this.name = name;
        this.color = color;
    }
    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getColor() {
        return color;
    }

    public static String getNameByCode(String code) {
        if (code == null) return null;
        for (MessageReadStatusEnums readStatus : values()) {
            if(readStatus.getCode() == code) {
                return readStatus.getName();
            }
        }
        return null;
    }
}
