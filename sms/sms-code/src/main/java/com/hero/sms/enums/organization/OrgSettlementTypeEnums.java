package com.hero.sms.enums.organization;

import com.hero.sms.enums.BaseEnum;

/**
 * 商户计算方式
 * @author Lenovo
 *
 */
public enum OrgSettlementTypeEnums implements BaseEnum {

	Prepayment("0","预付费"),UsedPayment("1","后付费");
	
	private String code;
	private String name;
	private OrgSettlementTypeEnums(String code, String name) {
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
