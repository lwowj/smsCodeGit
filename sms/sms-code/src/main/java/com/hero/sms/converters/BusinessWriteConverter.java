package com.hero.sms.converters;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.common.Code;
import com.wuwenze.poi.convert.WriteConverter;
import com.wuwenze.poi.exception.ExcelKitWriteConverterException;

/**
 * 商务ID转换
 */
public class BusinessWriteConverter implements WriteConverter {

    @Override
    public String convert(Object o) throws ExcelKitWriteConverterException {
        String businessUserId = (String) o;
        Code code = DatabaseCache.getCodeBySortCodeAndCode("Business",businessUserId);
        if(code != null){
            return code.getName();
        }
        return null;
    }
}
