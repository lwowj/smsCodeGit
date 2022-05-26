package com.hero.sms.enums.message;

import com.hero.sms.enums.BaseEnum;

/**
 * 消息类型
 * @author Lenovo
 *
 */
public enum SmsTypeEnums  implements BaseEnum {

	TextMsg(1,"短信");
	
	private int code;
	private String name;
	private SmsTypeEnums(int code, String name) {
		this.code = code;
		this.name = name;
	}
	public int getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	
	public static String getNameByCode(Integer code) {
		if (code == null) return null;
		for (SmsTypeEnums smsTypeEnums : values()) {
			if(smsTypeEnums.getCode() == code) {
				return smsTypeEnums.getName();
			}
		}
		return null;
	}
}
