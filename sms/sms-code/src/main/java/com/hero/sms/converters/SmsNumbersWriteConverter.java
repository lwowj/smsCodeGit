package com.hero.sms.converters;

import org.apache.commons.lang3.StringUtils;

import com.wuwenze.poi.convert.WriteConverter;
import com.wuwenze.poi.exception.ExcelKitWriteConverterException;

/**
 * 写文件时 手机号码串
 * 1 "、"替换","，防止EXCEL 将号码串识别为大数字
 * 2 屏蔽文件的路径
 */
public class SmsNumbersWriteConverter implements WriteConverter {
    @Override
    public String convert(Object o) throws ExcelKitWriteConverterException {
        String target = (String) o;

     /*   //文件上传    显示文件名
        String path = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
        if (StringUtils.isNotBlank(target) && target.contains(path)){
            return target.replace(path,"");
        }*/
        if (StringUtils.isNotBlank(target) && (target.endsWith(".xls") || target.endsWith(".xlsx")) ){
            return "";
        }
        return target.replaceAll(",","、");
    }
}
