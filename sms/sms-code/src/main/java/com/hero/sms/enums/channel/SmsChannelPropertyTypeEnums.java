package com.hero.sms.enums.channel;

import com.hero.sms.enums.BaseEnum;

/**
 * 短信通道属性类型
 * @author Lenovo
 *
 */
public enum SmsChannelPropertyTypeEnums implements BaseEnum {

	PushProp(1,"推送属性"),CostProp(2,"资费属性");
	
	private int code;
	private String name;
	
	private SmsChannelPropertyTypeEnums(int code, String name) {
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
