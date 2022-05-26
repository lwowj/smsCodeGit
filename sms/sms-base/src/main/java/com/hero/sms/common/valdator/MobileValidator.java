package com.hero.sms.common.valdator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.hero.sms.common.annotation.IsMobile;
import com.hero.sms.common.entity.Regexp;
import com.hero.sms.common.utils.AppUtil;

/**
 * 校验是否为合法的手机号码
 *
 * @author Administrator
 */
public class MobileValidator implements ConstraintValidator<IsMobile, String> {

    @Override
    public void initialize(IsMobile isMobile) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        try {
            if (StringUtils.isBlank(s)) {
                return true;
            } else {
            	String regex = Regexp.MOBILE_REG;
            	String[] mobiles = s.split(",");
            	for (String mobile : mobiles) {
            		if(!AppUtil.match(regex, mobile))return false;
				}
            	return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
