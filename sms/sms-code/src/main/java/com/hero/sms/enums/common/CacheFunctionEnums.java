package com.hero.sms.enums.common;

public enum CacheFunctionEnums {
	All("All","加载所有缓存"),
	BlackIp("BlackIp","IP白名单"),
	Code("Code","字典表"),
	SensitiveWord("SensitiveWord","敏感词");
	private String name;
	private String code;
	private CacheFunctionEnums(String code,String name) {
		this.name = name;
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public static CacheFunctionEnums getEnumByCode(String code) {
        if (code == null) return null;
        for (CacheFunctionEnums cacheFunctionEnums : values()) {
            if(cacheFunctionEnums.getCode() == code) {
                return cacheFunctionEnums;
            }
        }
        return null;
    }
	
}
