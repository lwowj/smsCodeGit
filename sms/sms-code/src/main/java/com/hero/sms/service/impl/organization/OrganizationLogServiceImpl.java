package com.hero.sms.service.impl.organization;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.AddressUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.organization.OrganizationLog;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.mapper.organization.OrganizationLogMapper;
import com.hero.sms.service.organization.IOrganizationLogService;

/**
 * 商户操作日志表 Service实现
 *
 * @author Administrator
 * @date 2020-03-21 23:29:41
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationLogServiceImpl extends ServiceImpl<OrganizationLogMapper, OrganizationLog> implements IOrganizationLogService {

    @Autowired
    private OrganizationLogMapper organizationLogMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public IPage<OrganizationLog> findOrganizationLogs(QueryRequest request, OrganizationLog organizationLog, Date organizationLogStartTime, Date organizationLogEndTime) {
        LambdaQueryWrapper<OrganizationLog> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(organizationLog.getUsername())){
            queryWrapper.like(OrganizationLog::getUsername,organizationLog.getUsername());
        }
        if(StringUtils.isNotEmpty(organizationLog.getMethod())){
            queryWrapper.like(OrganizationLog::getMethod,organizationLog.getMethod());
        }
        if(StringUtils.isNotEmpty(organizationLog.getOperation())){
            queryWrapper.like(OrganizationLog::getOperation,organizationLog.getOperation());
        }
        if(organizationLogStartTime != null){
            queryWrapper.ge(OrganizationLog::getCreateTime,organizationLogStartTime);
        }else{
            queryWrapper.ge(OrganizationLog::getCreateTime, DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(organizationLogEndTime != null){
            queryWrapper.le(OrganizationLog::getCreateTime,organizationLogEndTime);
        }else {
            queryWrapper.le(OrganizationLog::getCreateTime,DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        queryWrapper.orderByDesc(OrganizationLog::getId);
        Page<OrganizationLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationLog> findOrganizationLogs(OrganizationLog organizationLog) {
	    LambdaQueryWrapper<OrganizationLog> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationLog(OrganizationLog organizationLog) {
        this.save(organizationLog);
    }

    @Override
    @Transactional
    public void updateOrganizationLog(OrganizationLog organizationLog) {
        this.saveOrUpdate(organizationLog);
    }

    @Override
    @Transactional
    public void deleteOrganizationLog(OrganizationLog organizationLog) {
        LambdaQueryWrapper<OrganizationLog> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationLogs(String[] organizationLogIds) {
        List<String> list = Arrays.asList(organizationLogIds);
        this.removeByIds(list);
    }

    /**
     * 保存商户用户操作日志
     * @param point
     * @param method
     * @param ip
     * @param operation
     * @param start
     */
    @Override
    public void saveLog(ProceedingJoinPoint point, Method method, String ip , String operation, long start,OrganizationUserExt user) {
        OrganizationLog organizationLog = new OrganizationLog();
        // 设置 IP地址
        organizationLog.setIp(ip);
        if (user != null)
            organizationLog.setUsername(user.getUserAccount());
        // 设置耗时
        organizationLog.setTime(System.currentTimeMillis() - start);
        // 设置操作描述
        organizationLog.setOperation(operation);
        // 请求的类名
        String className = point.getTarget().getClass().getName();
        // 请求的方法名
        String methodName = method.getName();
        organizationLog.setMethod(className + "." + methodName + "()");
        // 请求的方法参数值
        Object[] args = point.getArgs();
        // 请求的方法参数名称
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null) {
            StringBuilder params = new StringBuilder();
            params = handleParams(params, args, Arrays.asList(paramNames));
            if("login".equals(methodName)){
                organizationLog.setParams(params.toString().replaceAll("password: \"(\\S)*\"", "password: \"保密\""));
            }else {
                organizationLog.setParams(params.toString());
            }
        }
        organizationLog.setCreateTime(new Date());
        organizationLog.setLocation(AddressUtil.getCityInfo(ip));
        // 保存系统日志
        save(organizationLog);
    }

    @SuppressWarnings("all")
    private StringBuilder handleParams(StringBuilder params, Object[] args, List paramNames) {
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Map) {
                    Set set = ((Map) args[i]).keySet();
                    List<Object> list = new ArrayList<>();
                    List<Object> paramList = new ArrayList<>();
                    for (Object key : set) {
                        list.add(((Map) args[i]).get(key));
                        paramList.add(key);
                    }
                    return handleParams(params, list.toArray(), paramList);
                } else {
                    if (args[i] instanceof Serializable) {
                        Class<?> aClass = args[i].getClass();
                        try {
                            aClass.getDeclaredMethod("toString", new Class[]{null});
                            // 如果不抛出 NoSuchMethodException 异常则存在 toString 方法 ，安全的 writeValueAsString ，否则 走 Object的 toString方法
                            params.append(" ").append(paramNames.get(i)).append(": ").append(objectMapper.writeValueAsString(args[i]));
                        } catch (NoSuchMethodException e) {
                            params.append(" ").append(paramNames.get(i)).append(": ").append(objectMapper.writeValueAsString(args[i].toString()));
                        }
                    } else if (args[i] instanceof MultipartFile) {
                        MultipartFile file = (MultipartFile) args[i];
                        params.append(" ").append(paramNames.get(i)).append(": ").append(file.getName());
                    } else {
                        params.append(" ").append(paramNames.get(i)).append(": ").append(args[i]);
                    }
                }
            }
        } catch (Exception ignore) {
            params.append("参数解析失败");
        }
        return params;
    }
}
