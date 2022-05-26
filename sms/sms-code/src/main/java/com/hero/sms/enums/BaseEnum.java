package com.hero.sms.enums;

import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonFormat;

@JSONType(serializeEnumAsJavaBean = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface BaseEnum {

}
