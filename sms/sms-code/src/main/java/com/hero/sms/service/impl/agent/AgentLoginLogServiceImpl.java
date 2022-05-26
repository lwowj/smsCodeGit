package com.hero.sms.service.impl.agent;


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
import com.hero.sms.entity.agent.AgentLoginLog;
import com.hero.sms.entity.agent.AgentLoginLogQuery;
import com.hero.sms.mapper.agent.AgentLoginLogMapper;
import com.hero.sms.service.agent.IAgentLoginLogService;

/**
 * 代理登录日志表 Service实现
 *
 * @author Administrator
 * @date 2020-12-25 17:32:55
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentLoginLogServiceImpl extends ServiceImpl<AgentLoginLogMapper, AgentLoginLog> implements IAgentLoginLogService {

    @Autowired
    private AgentLoginLogMapper agentLoginLogMapper;

    @Override
    public IPage<AgentLoginLog> findAgentLoginLogs(QueryRequest request, AgentLoginLog agentLoginLog) {
        LambdaQueryWrapper<AgentLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(agentLoginLog.getAgentAccount()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getAgentAccount,agentLoginLog.getAgentAccount());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getLocalIp,agentLoginLog.getLocalIp());
	    }
	    if(agentLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(AgentLoginLog::getLoginState,agentLoginLog.getLoginState());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getMessage()))
	    {
	    	queryWrapper.like(AgentLoginLog::getMessage,agentLoginLog.getMessage());
	    }
	    queryWrapper.orderByDesc(AgentLoginLog::getCreateTime);
        // TODO 设置查询条件
        Page<AgentLoginLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentLoginLog> findAgentLoginLogs(AgentLoginLog agentLoginLog) {
	    LambdaQueryWrapper<AgentLoginLog> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(agentLoginLog.getAgentAccount()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getAgentAccount,agentLoginLog.getAgentAccount());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getLocalIp,agentLoginLog.getLocalIp());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getMessage()))
	    {
	    	queryWrapper.like(AgentLoginLog::getMessage,agentLoginLog.getMessage());
	    }
	    if(agentLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(AgentLoginLog::getLoginState,agentLoginLog.getLoginState());
	    }
	    queryWrapper.orderByDesc(AgentLoginLog::getCreateTime);
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public IPage<AgentLoginLog> findAgentLoginLogs(QueryRequest request, AgentLoginLogQuery agentLoginLog) {
        LambdaQueryWrapper<AgentLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(agentLoginLog.getAgentAccount()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getAgentAccount,agentLoginLog.getAgentAccount());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getLocalIp,agentLoginLog.getLocalIp());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getAgentAccountFuzzy()))
	    {
	    	queryWrapper.like(AgentLoginLog::getAgentAccount,agentLoginLog.getAgentAccountFuzzy());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getLocalIpFuzzy()))
	    {
	    	queryWrapper.like(AgentLoginLog::getLocalIp,agentLoginLog.getLocalIpFuzzy());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getMessage()))
	    {
	    	queryWrapper.like(AgentLoginLog::getMessage,agentLoginLog.getMessage());
	    }
	    if(agentLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(AgentLoginLog::getLoginState,agentLoginLog.getLoginState());
	    }
	    if (agentLoginLog.getCreateStartTime() != null){//提交开始时间
        	queryWrapper.ge(AgentLoginLog::getCreateTime,agentLoginLog.getCreateStartTime());
		}
		if (agentLoginLog.getCreateEndTime() != null) {//提交结束时间
			queryWrapper.le(AgentLoginLog::getCreateTime, agentLoginLog.getCreateEndTime());
		}
	   //创建时间的排序方式
	   	if(agentLoginLog.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(agentLoginLog.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(AgentLoginLog::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(AgentLoginLog::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(AgentLoginLog::getCreateTime);
	   	}
        // TODO 设置查询条件
        Page<AgentLoginLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentLoginLog> findAgentLoginLogs(AgentLoginLogQuery agentLoginLog) {
	    LambdaQueryWrapper<AgentLoginLog> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(agentLoginLog.getAgentAccount()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getAgentAccount,agentLoginLog.getAgentAccount());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getLocalIp()))
	    {
	    	queryWrapper.eq(AgentLoginLog::getLocalIp,agentLoginLog.getLocalIp());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getAgentAccountFuzzy()))
	    {
	    	queryWrapper.like(AgentLoginLog::getAgentAccount,agentLoginLog.getAgentAccountFuzzy());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getLocalIpFuzzy()))
	    {
	    	queryWrapper.like(AgentLoginLog::getLocalIp,agentLoginLog.getLocalIpFuzzy());
	    }
	    if(StringUtils.isNotBlank(agentLoginLog.getMessage()))
	    {
	    	queryWrapper.like(AgentLoginLog::getMessage,agentLoginLog.getMessage());
	    }
	    if(agentLoginLog.getLoginState()!=null)
	    {
	    	queryWrapper.eq(AgentLoginLog::getLoginState,agentLoginLog.getLoginState());
	    }
	    if (agentLoginLog.getCreateStartTime() != null){//提交开始时间
        	queryWrapper.ge(AgentLoginLog::getCreateTime,agentLoginLog.getCreateStartTime());
		}
		if (agentLoginLog.getCreateEndTime() != null) {//提交结束时间
			queryWrapper.le(AgentLoginLog::getCreateTime, agentLoginLog.getCreateEndTime());
		}
	   //创建时间的排序方式
	   	if(agentLoginLog.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(agentLoginLog.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(AgentLoginLog::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(AgentLoginLog::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(AgentLoginLog::getCreateTime);
	   	}
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }
    
    @Override
    @Transactional
    public void createAgentLoginLog(AgentLoginLog agentLoginLog) {
        this.save(agentLoginLog);
    }

    @Override
    @Transactional
    public void updateAgentLoginLog(AgentLoginLog agentLoginLog) {
        this.saveOrUpdate(agentLoginLog);
    }

    @Override
    @Transactional
    public void deleteAgentLoginLog(AgentLoginLog agentLoginLog) {
        LambdaQueryWrapper<AgentLoginLog> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteAgentLoginLogs(String[] agentLoginLogIds) {
        List<String> list = Arrays.asList(agentLoginLogIds);
        this.removeByIds(list);
    }
    
    @Override
    @Transactional
    public void saveLog(long start, AgentLoginLog agentLoginLog)
    {
    	// 设置耗时
    	agentLoginLog.setTimeConsuming(System.currentTimeMillis() - start);
    	// 保存系统日志
        save(agentLoginLog);
    }
}
