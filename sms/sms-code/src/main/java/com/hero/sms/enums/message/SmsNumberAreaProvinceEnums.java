package com.hero.sms.enums.message;

import org.apache.commons.lang3.StringUtils;

/**
 * 手机号码归属省份
 */
public enum  SmsNumberAreaProvinceEnums{

	BeiJing("1","北京","+86"),
	Tianjin("2","天津","+86"),
	HeBei("3","河北","+86"),
	LiaoNing("4","辽宁","+86"),
	JiLin("5","吉林","+86"),
	HeiLongJiang("6","黑龙江","+86"),
	ShanDong("7","山东","+86"),
	JiangSu("8","江苏","+86"),
	ShangHai("9","上海","+86"),
	ZheJiang("10","浙江","+86"),
	AnHui("11","安徽","+86"),
	FuJian("12","福建","+86"),
	JiangXi("13","江西","+86"),
	GuangDong("14","广东","+86"),
	GuangXi("15","广西","+86"),
	HaiNan("16","海南","+86"),
	HeNan("17","河南","+86"),
	HuNan("18","湖南","+86"),
	HuBei("19","湖北","+86"),
	ShanXi("20","山西","+86"),
	NeiMengGu("21","内蒙古","+86"),
	NingXia("22","宁夏","+86"),
	QingHai("23","青海","+86"),
	ShanXiS("24","陕西","+86"),
	GanSu("25","甘肃","+86"),
	XinJiang("26","新疆","+86"),
	SiChuan("27","四川","+86"),
	GuiZhou("28","贵州","+86"),
	YunNan("29","云南","+86"),
	ChongQing("30","重庆","+86"),
	XiZang("31","西藏","+86");
	
	private String code;
	
	private String name;
	
	private String areaCode;

	private SmsNumberAreaProvinceEnums(String code, String name, String areaCode) {
		this.code = code;
		this.name = name;
		this.areaCode = areaCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public static String getCodeByAreaCodeAndName(String areaCode,String provinceName) {
		for (SmsNumberAreaProvinceEnums provinceEnums : values()) {
			if(provinceEnums.getAreaCode().equals(areaCode) && provinceEnums.getName().equals(provinceName)) {
				return provinceEnums.getCode();
			}
		}
		return null;
	}

	public static String getNameByCode(String code) {
		if (StringUtils.isBlank(code))return null;
		for (SmsNumberAreaProvinceEnums provinceEnums : values()) {
			if(provinceEnums.getCode().equals(code)) {
				return provinceEnums.getName();
			}
		}
		return null;
	}
}
