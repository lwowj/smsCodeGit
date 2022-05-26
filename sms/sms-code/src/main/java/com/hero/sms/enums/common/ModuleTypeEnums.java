package com.hero.sms.enums.common;



public enum ModuleTypeEnums {

    All(0,"全部"),SensitiveWord(1,"敏感词"),CodeList(2,"字典信息"),AgentList(3,"代理信息"),OrganizationList(4,"商户信息"),
    SmsChannelList(5,"短信通道信息"),PayChannelList(6,"支付通道信息"),BlackIpConfig(7,"IP黑名单"),MobileAreaList(8,"手机号段区域信息"),
    MobileBlack(9,"号码黑名单"),GatewayType(10,"网关类型信息"),AreaCodeList(11,"网关类型信息");

    private Integer code;
    private String name;
    private ModuleTypeEnums(Integer code, String name) {
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
        for (ModuleTypeEnums moduleTypeEnums : values()) {
            if(moduleTypeEnums.getCode().intValue() == code.intValue()) {
                return moduleTypeEnums.getName();
            }
        }
        return null;
    }
}
