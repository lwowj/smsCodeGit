package com.hero.sms.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.hero.sms.common.valdator.DateValidator;

/**
 * @author Administrator
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
public @interface IsDate {

    String message();
    
    String pattern();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
