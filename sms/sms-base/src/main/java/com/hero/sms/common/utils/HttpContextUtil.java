package com.hero.sms.common.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Administrator
 */
public class HttpContextUtil {

	private HttpContextUtil(){

	}
	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
	}
}
