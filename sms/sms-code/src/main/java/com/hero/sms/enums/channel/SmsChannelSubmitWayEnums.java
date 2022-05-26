package com.hero.sms.enums.channel;

import com.hero.sms.enums.BaseEnum;

/**
 * 短信通道提交方式
 * @author Lenovo
 *
 */
public enum SmsChannelSubmitWayEnums implements BaseEnum 
{

	SYNC(0,"同步提交","green"),ASYN(1,"异步提交","blue");
	
	private int code;
	private String name;
	private String color;
	private SmsChannelSubmitWayEnums(int code, String name, String color) {
		this.code = code;
		this.name = name;
		this.color = color;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

}
