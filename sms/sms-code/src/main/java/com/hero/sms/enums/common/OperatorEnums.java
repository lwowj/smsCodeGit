package com.hero.sms.enums.common;

/**
 * 运营商
 * @author Lenovo
 *
 */
public enum OperatorEnums {

	CTCC("CTCC","电信"),CUCC("CUCC","联通"),CMCC("CMCC","移动");
	
	private String code;
	private String name;
	private OperatorEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}

	public static String getNameByCode(String code) {
		if (code == null) return null;
		for (OperatorEnums operatorEnums : values()) {
			if(operatorEnums.getCode().equals(code)) {
				return operatorEnums.getName();
			}
		}
		return null;
	}

}
