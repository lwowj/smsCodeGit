package com.hero.sms.monitor.service.impl;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.sms.common.entity.Constant;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.AddressUtil;
import com.hero.sms.common.utils.SortUtil;
import com.hero.sms.monitor.entity.SystemLog;
import com.hero.sms.monitor.mapper.LogMapper;
import com.hero.sms.monitor.service.ILogService;
import com.hero.sms.system.entity.User;

/**
 * @author Administrator
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LogServiceImpl extends ServiceImpl<LogMapper, SystemLog> implements ILogService {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public IPage<SystemLog> findLogs(SystemLog systemLog, QueryRequest request) {
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(systemLog.getUsername())) {
            queryWrapper.lambda().eq(SystemLog::getUsername, systemLog.getUsername().toLowerCase());
        }
        if (StringUtils.isNotBlank(systemLog.getOperation())) {
            queryWrapper.lambda().like(SystemLog::getOperation, systemLog.getOperation());
        }
        if (StringUtils.isNotBlank(systemLog.getLocation())) {
            queryWrapper.lambda().like(SystemLog::getLocation, systemLog.getLocation());
        }
        if (StringUtils.isNotBlank(systemLog.getCreateTimeFrom()) && StringUtils.isNotBlank(systemLog.getCreateTimeTo())) {
            queryWrapper.lambda()
                    .ge(SystemLog::getCreateTime, systemLog.getCreateTimeFrom())
                    .le(SystemLog::getCreateTime, systemLog.getCreateTimeTo());
        }

        Page<SystemLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "createTime", Constant.ORDER_DESC, true);

        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public void deleteLogs(String[] logIds) {
        List<String> list = Arrays.asList(logIds);
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public void saveLog(ProceedingJoinPoint point, Method method, String ip , String operation, long start,User user) {
        SystemLog systemLog = new SystemLog();
        // ?????? IP??????
        systemLog.setIp(ip);
        if (user != null)
            systemLog.setUsername(user.getUsername());
        // ????????????
        systemLog.setTime(System.currentTimeMillis() - start);
        // ??????????????????
        systemLog.setOperation(operation);
        // ???????????????
        String className = point.getTarget().getClass().getName();
        // ??????????????????
        String methodName = method.getName();
        systemLog.setMethod(className + "." + methodName + "()");
        // ????????????????????????
        Object[] args = point.getArgs();
        // ???????????????????????????
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null) {
            StringBuilder params = new StringBuilder();
            params = handleParams(params, args, Arrays.asList(paramNames));
            systemLog.setParams(params.toString());
        }
        systemLog.setCreateTime(new Date());
        systemLog.setLocation(AddressUtil.getCityInfo(ip));
        // ??????????????????
        save(systemLog);
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
                            // ??????????????? NoSuchMethodException ??????????????? toString ?????? ???????????? writeValueAsString ????????? ??? Object??? toString??????
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
            params.append("??????????????????");
        }
        return params;
    }
}
