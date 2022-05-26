package com.hero.sms.enums.common;

/**
 * 处理情况状态
 * @author Lenovo
 *
 */
public enum ProcessingStateEnums {

	UNFINISHED(0,"未处理"),FINISHED(1,"已处理"),INVALID(2,"无效");
	
	private Integer code;
	private String name;
	private ProcessingStateEnums(Integer code, String name) {
		this.code = code;
		this.name = name;
	}
	public Integer getCode() {
		return code;
	}
	public String getName() {
		return name;
	}

	public static String getNameByCode(Integer code) {
		if (code == null) return null;
		for (ProcessingStateEnums commonStateEnums : values()) {
			if(commonStateEnums.getCode() == code) {
				return commonStateEnums.getName();
			}
		}
		return null;
	}

}
