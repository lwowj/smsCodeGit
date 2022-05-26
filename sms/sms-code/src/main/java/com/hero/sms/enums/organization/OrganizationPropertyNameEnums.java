package com.hero.sms.enums.organization;

public enum OrganizationPropertyNameEnums {

	SystemId("systemId","systemId"),
	Password("password","密码"),
	MaxChannels("maxChannels","最大连接数"),
	ReadLimit("readLimit","流速");
	
	
	private String code;
	private String name;
	private OrganizationPropertyNameEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	
}
