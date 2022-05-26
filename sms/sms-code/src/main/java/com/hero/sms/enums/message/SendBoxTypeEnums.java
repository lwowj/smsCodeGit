package com.hero.sms.enums.message;

import com.hero.sms.enums.BaseEnum;

/**
 * 发件箱类型
 */
public enum SendBoxTypeEnums implements BaseEnum {

    formSubmit(1,"普通表单提交"),excleSubmit(2,"excel模板提交");

    private int code;

    private String name;

    SendBoxTypeEnums(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
