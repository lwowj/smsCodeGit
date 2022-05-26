package com.hero.sms.enums.blackIpConfig;

/**
 * 启用状态
 * @author Lenovo
 *
 */
public enum IsAvailabilityEnums {

	Invoke(1,"启用","green"),NoInvoke(0,"未启用","volcano");

	private Integer code;
	private String name;
	private String color;
	private IsAvailabilityEnums(Integer code, String name, String color) {
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
		for (IsAvailabilityEnums auditStateEnums : values()) {
			if(auditStateEnums.getCode() == code) {
				return auditStateEnums.getName();
			}
		}
		return null;
	}
}
