package com.hero.sms.enums.channel;

import com.hero.sms.enums.BaseEnum;

/**
 * 短信通道协议类型
 * @author Lenovo
 *
 */
public enum SmsChannelProtocolTypeEnums implements BaseEnum {

	Http("http","HTTP协议","green"),Smpp("smpp","SMPP协议","blue");
	
	private String code;
	private String name;
	private String color;
	
	private SmsChannelProtocolTypeEnums(String code, String name, String color) {
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
}
