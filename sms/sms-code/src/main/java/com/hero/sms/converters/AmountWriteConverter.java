package com.hero.sms.converters;

import java.math.BigDecimal;

import com.wuwenze.poi.convert.WriteConverter;
import com.wuwenze.poi.exception.ExcelKitWriteConverterException;

public class AmountWriteConverter implements WriteConverter {
    @Override
    public String convert(Object o) throws ExcelKitWriteConverterException {
        BigDecimal divideNum = new BigDecimal(100);
        BigDecimal oBigDecimal = null;
        if (o instanceof Integer){
            oBigDecimal = new BigDecimal((Integer) o);
        }else if (o instanceof String){
            oBigDecimal = new BigDecimal((String) o);
        }else if (o instanceof Long){
            oBigDecimal = new BigDecimal((Long) o);
        }else if (o instanceof Double){
            oBigDecimal = new BigDecimal((Double)o);
        }else if (o instanceof Float){
            oBigDecimal = new BigDecimal((Float)o);
        }else {
            return "";
        }

        return oBigDecimal.divide(divideNum).toString();
    }
}
