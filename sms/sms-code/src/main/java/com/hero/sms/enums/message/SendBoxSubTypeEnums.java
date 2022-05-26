package com.hero.sms.enums.message;

/**
 * 发件箱提交方式
 * @author Lenovo
 *
 */
public enum SendBoxSubTypeEnums {

	OrgWebSub(1,"商户后台"),HttpSub(2,"http接口"),SmppSub(4,"smpp接口");
	
	private Integer code;
	private String name;
	private SendBoxSubTypeEnums(Integer code, String name) {
		this.code = code;
		this.name = name;
	}
	public Integer getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
}
