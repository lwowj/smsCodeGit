package com.hero.sms.enums.common;

import com.hero.sms.enums.BaseEnum;

public enum ExportTypeEnums implements BaseEnum {
    ExportSendRecord("1","导出发送记录"),ExportSendBox("2","导出发件箱");

    private String code;
    private String name;
    private ExportTypeEnums(String code, String name) {
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
        for (ExportTypeEnums exportTypeEnums : values()) {
            if(exportTypeEnums.getCode().equals(code)) {
                return exportTypeEnums.getName();
            }
        }
        return null;
    }
}
