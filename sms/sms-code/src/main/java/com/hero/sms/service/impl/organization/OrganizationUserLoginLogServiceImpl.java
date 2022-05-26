package com.hero.sms.service.impl.organization;


import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationUserLoginLog;
import com.hero.sms.entity.organization.OrganizationUserLoginLogQuery;
import com.hero.sms.mapper.organization.OrganizationUserLoginLogMapper;
import com.hero.sms.service.organization.IOrganizationUserLoginLogService;

/**
 * 商户用户登录日志表 Service实现
 *
 * @author Administrator
 * @date 2020-12-25 17:33:01
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationUserLoginLogServiceImpl extends ServiceImpl<OrganizationUserLoginLogMapper, OrganizationUserLoginLog> implements IOrganizationUserLoginLogService {

    @Autowired
    private OrganizationUserLoginLogMapper organizationUserLoginLogMapper;

    @Override
    public IPage<OrganizationUserLoginLog> findOrganizationUserLoginLogs(QueryRequest request, OrganizationUserLoginLog organizationUserLoginLog) {
        LambdaQueryWrapper<OrganizationUserLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(organizationUserLoginLog.getOrganizationCode()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getOrganizationCode,organizationUserLoginLog.getOrganizationCode());
	    }
        if(StringUtils.isNotBlank(organizationUserLoginLog.getUserAccount()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getUserAccount,organizationUserLoginLog.getUserAccount());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLocalIp,organizationUserLoginLog.getLocalIp());
	    }
	    if(organizationUserLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLoginState,organizationUserLoginLog.getLoginState());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getMessage()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getMessage,organizationUserLoginLog.getMessage());
	    }
	    queryWrapper.orderByDesc(OrganizationUserLoginLog::getCreateTime);
        // TODO 设置查询条件
        Page<OrganizationUserLoginLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationUserLoginLog> findOrganizationUserLoginLogs(OrganizationUserLoginLog organizationUserLoginLog) {
	    LambdaQueryWrapper<OrganizationUserLoginLog> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getOrganizationCode()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getOrganizationCode,organizationUserLoginLog.getOrganizationCode());
	    }
        if(StringUtils.isNotBlank(organizationUserLoginLog.getUserAccount()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getUserAccount,organizationUserLoginLog.getUserAccount());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLocalIp,organizationUserLoginLog.getLocalIp());
	    }
	    if(organizationUserLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLoginState,organizationUserLoginLog.getLoginState());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getMessage()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getMessage,organizationUserLoginLog.getMessage());
	    }
	    queryWrapper.orderByDesc(OrganizationUserLoginLog::getCreateTime);
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public IPage<OrganizationUserLoginLog> findOrganizationUserLoginLogs(QueryRequest request, OrganizationUserLoginLogQuery organizationUserLoginLog) {
        LambdaQueryWrapper<OrganizationUserLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(organizationUserLoginLog.getOrganizationCode()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getOrganizationCode,organizationUserLoginLog.getOrganizationCode());
	    }
        if(StringUtils.isNotBlank(organizationUserLoginLog.getUserAccount()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getUserAccount,organizationUserLoginLog.getUserAccount());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLocalIp,organizationUserLoginLog.getLocalIp());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getUserAccountFuzzy()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getUserAccount,organizationUserLoginLog.getUserAccountFuzzy());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getLocalIpFuzzy()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getLocalIp,organizationUserLoginLog.getLocalIpFuzzy());
	    }
	    if(organizationUserLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLoginState,organizationUserLoginLog.getLoginState());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getMessage()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getMessage,organizationUserLoginLog.getMessage());
	    }
	    if (organizationUserLoginLog.getCreateStartTime() != null){//提交开始时间
        	queryWrapper.ge(OrganizationUserLoginLog::getCreateTime,organizationUserLoginLog.getCreateStartTime());
		}
		if (organizationUserLoginLog.getCreateEndTime() != null) {//提交结束时间
			queryWrapper.le(OrganizationUserLoginLog::getCreateTime, organizationUserLoginLog.getCreateEndTime());
		}
	    //创建时间的排序方式
	   	if(organizationUserLoginLog.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(organizationUserLoginLog.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(OrganizationUserLoginLog::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(OrganizationUserLoginLog::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(OrganizationUserLoginLog::getCreateTime);
	   	}
        // TODO 设置查询条件
        Page<OrganizationUserLoginLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationUserLoginLog> findOrganizationUserLoginLogs(OrganizationUserLoginLogQuery organizationUserLoginLog) {
	    LambdaQueryWrapper<OrganizationUserLoginLog> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getOrganizationCode()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getOrganizationCode,organizationUserLoginLog.getOrganizationCode());
	    }
        if(StringUtils.isNotBlank(organizationUserLoginLog.getUserAccount()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getUserAccount,organizationUserLoginLog.getUserAccount());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLocalIp,organizationUserLoginLog.getLocalIp());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getUserAccountFuzzy()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getUserAccount,organizationUserLoginLog.getUserAccountFuzzy());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getLocalIpFuzzy()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getLocalIp,organizationUserLoginLog.getLocalIpFuzzy());
	    }
	    if(organizationUserLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(OrganizationUserLoginLog::getLoginState,organizationUserLoginLog.getLoginState());
	    }
	    if(StringUtils.isNotBlank(organizationUserLoginLog.getMessage()))
	    {
	    	queryWrapper.like(OrganizationUserLoginLog::getMessage,organizationUserLoginLog.getMessage());
	    }
	    if (organizationUserLoginLog.getCreateStartTime() != null){//提交开始时间
        	queryWrapper.ge(OrganizationUserLoginLog::getCreateTime,organizationUserLoginLog.getCreateStartTime());
		}
		if (organizationUserLoginLog.getCreateEndTime() != null) {//提交结束时间
			queryWrapper.le(OrganizationUserLoginLog::getCreateTime, organizationUserLoginLog.getCreateEndTime());
		}
	    //创建时间的排序方式
	   	if(organizationUserLoginLog.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(organizationUserLoginLog.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(OrganizationUserLoginLog::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(OrganizationUserLoginLog::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(OrganizationUserLoginLog::getCreateTime);
	   	}
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }
    
    @Override
    @Transactional
    public void createOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog) {
        this.save(organizationUserLoginLog);
    }

    @Override
    @Transactional
    public void updateOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog) {
        this.saveOrUpdate(organizationUserLoginLog);
    }

    @Override
    @Transactional
    public void deleteOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog) {
        LambdaQueryWrapper<OrganizationUserLoginLog> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationUserLoginLogs(String[] organizationUserLoginLogIds) {
        List<String> list = Arrays.asList(organizationUserLoginLogIds);
        this.removeByIds(list);
    }
    
    @Override
    @Transactional
    public void saveLog(long start, OrganizationUserLoginLog organizationUserLoginLog)
    {
    	// 设置耗时
    	organizationUserLoginLog.setTimeConsuming(System.currentTimeMillis() - start);
    	// 保存系统日志
        save(organizationUserLoginLog);
    }
}
