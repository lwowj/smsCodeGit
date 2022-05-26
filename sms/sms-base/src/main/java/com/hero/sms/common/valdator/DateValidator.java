package com.hero.sms.common.valdator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.hero.sms.common.annotation.IsDate;

/**
 * 校验日期是否正确
 *
 * @author Administrator
 */
public class DateValidator implements ConstraintValidator<IsDate, String> {

	private IsDate isDate; 
	
    @Override
    public void initialize(IsDate isDate) {
    	this.isDate = isDate;
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
    	if(StringUtils.isEmpty(s)) return true;
        try {
        	DateUtils.parseDate(s, isDate.pattern());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
