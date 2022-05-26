package com.hero.sms.converters;

import com.hero.sms.enums.message.SmsTypeEnums;
import com.wuwenze.poi.convert.WriteConverter;
import com.wuwenze.poi.exception.ExcelKitWriteConverterException;

public class SmsTypeWriteConverter implements WriteConverter {
    @Override
    public String convert(Object o) throws ExcelKitWriteConverterException {
        Integer target = null;
        if (o instanceof String){
            target = Integer.valueOf((String) o);
        }else if (o instanceof Integer){
            target = (Integer) o;
        }else {
            return null;
        }
        return SmsTypeEnums.getNameByCode(target);
    }
}
