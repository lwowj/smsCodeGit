package com.hero.sms.converters;

import com.hero.sms.common.DatabaseCache;
import com.wuwenze.poi.convert.WriteConverter;
import com.wuwenze.poi.exception.ExcelKitWriteConverterException;

public class SmsNumberOperatorWriteConverter  implements WriteConverter {
    @Override
    public String convert(Object o) throws ExcelKitWriteConverterException {
        String target;
        if (o instanceof Integer){
            target = String.valueOf(o);
        }else if (o instanceof String){
            target = (String) o;
        }else {
            return null;
        }
        return DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",target).getName();
    }
}
