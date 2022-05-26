package com.hero.sms.enums.message;

import com.hero.sms.enums.BaseEnum;

/**
 * 发件箱定时短信
 */
public enum SendBoxIsTimingEnums implements BaseEnum {

    isTiming(true,"是","blank"),theTiming(false,"否","green");

    private boolean code;
    private String name;
    private String color;
    SendBoxIsTimingEnums(boolean code, String name, String color) {
        this.code = code;
        this.name = name;
        this.color = color;
    }

    public boolean getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    
	public String getColor() {
		return color;
	}
}
