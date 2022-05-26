package com.hero.sms.converters;

import com.hero.sms.common.DatabaseCache;
import com.wuwenze.poi.convert.WriteConverter;
import com.wuwenze.poi.exception.ExcelKitWriteConverterException;

public class OrgNameWriteConverter implements WriteConverter {
    @Override
    public String convert(Object o) throws ExcelKitWriteConverterException {
        if (o instanceof String){
            String orgCode = (String) o;
            return DatabaseCache.getOrgNameByOrgcode(orgCode);
        }
        return null;
    }
}
