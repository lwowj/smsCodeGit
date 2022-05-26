package com.hero.sms.common.aspect;

import java.lang.reflect.Method;

import com.hero.sms.entity.agent.Agent;
import com.hero.sms.service.agent.IAgentLogService;
import com.hero.sms.utils.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.LimitAccessException;
import com.hero.sms.common.utils.AppUtil;

/**
 * @author Administrator
 */
@Aspect
@Component
public class ControllerEndpointAspect extends AspectSupport {

    @Autowired
    private IAgentLogService agentLogService;

    @Pointcut("@annotation(com.hero.sms.common.annotation.ControllerEndpoint)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws FebsException {
        Object result;
        Method targetMethod = resolveMethod(point);
        ControllerEndpoint annotation = targetMethod.getAnnotation(ControllerEndpoint.class);
        String operation = annotation.operation();
        long start = System.currentTimeMillis();
        try {
            result = point.proceed();
            if (StringUtils.isNotBlank(operation)) {
                RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) attributes;
                String ip = StringUtils.EMPTY;
                if (servletRequestAttributes != null) {
                    ip = servletRequestAttributes.getRequest().getRemoteAddr();
                }
                // 设置操作用户
                Agent user = (Agent) SecurityUtils.getSubject().getPrincipal();
                agentLogService.saveLog(point, targetMethod, ip, operation, start,user);
            }
            return result;
        }
        catch (Throwable throwable) {
            String exceptionMessage = annotation.exceptionMessage();
            String message = throwable.getMessage();
        	if(throwable.getCause()!=null&&throwable.getCause() instanceof LimitAccessException)
        	{
        		exceptionMessage = "请求访问过快";
        		message = throwable.getCause().getMessage();
        	}
        	if(StringUtil.isEmpty(message))
            {
            	message = "";
            }
            String error = AppUtil.containChinese(message) ? exceptionMessage + "，" + message : exceptionMessage;
            throw new FebsException(error);
        }
    }
}



