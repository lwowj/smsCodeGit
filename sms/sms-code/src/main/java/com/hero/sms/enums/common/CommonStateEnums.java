package com.hero.sms.enums.common;

/**
 * 通用状态
 * @author Lenovo
 *
 */
public enum CommonStateEnums {

	SUCCESS(1,"成功","green"),FAIL(0,"失败","volcano");
	
	private Integer code;
	private String name;
	private String color;
	private CommonStateEnums(Integer code, String name, String color) {
		this.code = code;
		this.name = name;
		this.color = color;
	}
	public Integer getCode() {
		return code;
	}
	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}
	public static String getNameByCode(Integer code) {
		if (code == null) return null;
		for (CommonStateEnums commonStateEnums : values()) {
			if(commonStateEnums.getCode() == code) {
				return commonStateEnums.getName();
			}
		}
		return null;
	}

}
