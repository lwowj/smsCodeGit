package com.hero.sms.enums.message;

/**
 * 长短信状态
 * @author Lenovo
 *
 */
public enum SmsContentLengthStateEnums {

	No(1,"否","blank"),Yes(2,"是","green");

	private Integer code;
	private String name;
	private String color;
	private SmsContentLengthStateEnums(Integer code, String name, String color) {
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
		for (SmsContentLengthStateEnums smsContentLengthStateEnums : values()) {
			if(smsContentLengthStateEnums.getCode() == code) {
				return smsContentLengthStateEnums.getName();
			}
		}
		return null;
	}
}
