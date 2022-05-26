package com.hero.sms.enums.organization;

import com.hero.sms.enums.BaseEnum;

/**
 * 商户资费状态
 * @author Lenovo
 *
 */
public enum OrgCostStateEnums implements BaseEnum {

	Normal("1","正常","green"),Lock("0","锁定","volcano");
	private String code;
	private String name;
	private String color;
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getColor() {
		return color;
	}
	private OrgCostStateEnums(String code, String name, String color) {
		this.code = code;
		this.name = name;
		this.color = color;
	}
}
