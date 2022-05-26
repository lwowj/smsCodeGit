package com.hero.sms.enums.common;

/**
 * 审核状态
 * @author Lenovo
 *
 */
public enum AuditStateEnums {

	Wait(1,"待审核","blank"),Pass(2,"审核通过","green"),NoPass(4,"审核不通过","volcano");

	private Integer code;
	private String name;
	private String color;
	private AuditStateEnums(Integer code, String name, String color) {
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
		for (AuditStateEnums auditStateEnums : values()) {
			if(auditStateEnums.getCode() == code) {
				return auditStateEnums.getName();
			}
		}
		return null;
	}
}
