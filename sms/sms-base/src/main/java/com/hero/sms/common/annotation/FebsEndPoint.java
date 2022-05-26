package com.hero.sms.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Administrator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface FebsEndPoint {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
