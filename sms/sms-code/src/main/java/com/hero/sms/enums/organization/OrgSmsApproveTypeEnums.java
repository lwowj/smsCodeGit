package com.hero.sms.enums.organization;

import com.hero.sms.enums.BaseEnum;

/**
 * 商户短信审核类型
 * @author Lenovo
 *
 */
public enum OrgSmsApproveTypeEnums implements BaseEnum {

	TempNotAudit("0","模板免审"),ManualAudit("1","人工审核"),NotAudit("2","免审");
	
	private String code;
	private String name;
	private OrgSmsApproveTypeEnums(String code, String name) {
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
