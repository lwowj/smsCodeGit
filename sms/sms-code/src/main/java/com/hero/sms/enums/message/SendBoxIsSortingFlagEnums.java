package com.hero.sms.enums.message;

import com.hero.sms.enums.BaseEnum;

/**
 * 发件箱是否已分拣
 */
public enum SendBoxIsSortingFlagEnums implements BaseEnum {

	hasBeenSorting(true,"是","green"),notSorting(false,"否","blank");

    private boolean code;
    private String name;
    private String color;
    SendBoxIsSortingFlagEnums(boolean code, String name, String color) {
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
