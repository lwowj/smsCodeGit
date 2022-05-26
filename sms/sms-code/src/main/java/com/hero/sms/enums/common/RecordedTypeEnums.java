package com.hero.sms.enums.common;

import com.hero.sms.enums.BaseEnum;

/**
 * 入账方式
 * @author Lenovo
 *
 */
public enum RecordedTypeEnums implements BaseEnum {

	Booked("0","入账","green"),UnBooked("1","挂账","volcano"),Giving("2","赠送","blue");
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
	private RecordedTypeEnums(String code, String name, String color) 
	{
		this.code = code;
		this.name = name;
		this.color = color;
	}
	
	public static boolean verifyThereCode(String code) 
	{
		boolean verifyFlag = false;
		if (code == null||"".equals(code)) return false;
		for (RecordedTypeEnums recordedTypeEnums : values()) 
		{
			if(recordedTypeEnums.getCode().equals(code)) 
			{
				verifyFlag = true;
				break;
			}
		}
		return verifyFlag;
	}
	
	public static String getNameByCode(String code) 
	{
		if (code == null) return null;
		for (RecordedTypeEnums recordedTypeEnums : values()) {
			if(recordedTypeEnums.getCode().equals(code)) {
				return recordedTypeEnums.getName();
			}
		}
		return null;
	}
}
