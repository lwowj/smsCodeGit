package com.hero.sms.enums.channel;

import com.hero.sms.enums.BaseEnum;

public enum ChannelStateEnums implements BaseEnum {

	START(1,"启动","green"),Pause(2,"暂停","blue"),STOP(4,"停止","orange"),INVALID(5,"作废","red");
	
	private int code;
	private String name;
	private String color;
	private ChannelStateEnums(int code, String name, String color) {
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
