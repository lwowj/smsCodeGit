package com.hero.sms.enums.common;

/**
 * 强制绑定google状态
 * @author Lenovo
 *
 */
public enum NeedBindGoogleKeyEnums  {

	OPTIONAL(0,"非强制","volcano"),MANDATORY(1,"强制","green");
	
	private int code;
	private String name;
	private String color;
	public int getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getColor() {
		return color;
	}
	private NeedBindGoogleKeyEnums(int code, String name, String color) {
		this.code = code;
		this.name = name;
		this.color = color;
	}

	public static String getNameByCode(Integer code) {
		if (code == null) return null;
		for (NeedBindGoogleKeyEnums needBindGoogleKeyEnums : values()) {
			if(needBindGoogleKeyEnums.getCode() == code) {
				return needBindGoogleKeyEnums.getName();
			}
		}
		return null;
	}

}
