package com.hero.sms.service.impl.common;


import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.common.BlackIpConfig;
import com.hero.sms.entity.common.BlackIpConfigQuery;
import com.hero.sms.mapper.common.BlackIpConfigMapper;
import com.hero.sms.service.common.IBlackIpConfigService;

/**
 *  Service实现
 *
 * @author Administrator
 * @date 2020-12-21 19:43:55
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class BlackIpConfigServiceImpl extends ServiceImpl<BlackIpConfigMapper, BlackIpConfig> implements IBlackIpConfigService {

    @Autowired
    private BlackIpConfigMapper blackIpConfigMapper;

    @Override
    public IPage<BlackIpConfig> findBlackIpConfigs(QueryRequest request, BlackIpConfig blackIpConfig) {
        LambdaQueryWrapper<BlackIpConfig> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(blackIpConfig.getBlackIp()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getBlackIp,blackIpConfig.getBlackIp());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getLimitProject()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getLimitProject,blackIpConfig.getLimitProject());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getIsavailability()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getIsavailability,blackIpConfig.getIsavailability());
	    }
	    queryWrapper.orderByDesc(BlackIpConfig::getCreateDate);
        // TODO 设置查询条件
        Page<BlackIpConfig> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<BlackIpConfig> findBlackIpConfigs(BlackIpConfig blackIpConfig) {
	    LambdaQueryWrapper<BlackIpConfig> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(blackIpConfig.getBlackIp()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getBlackIp,blackIpConfig.getBlackIp());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getLimitProject()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getLimitProject,blackIpConfig.getLimitProject());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getIsavailability()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getIsavailability,blackIpConfig.getIsavailability());
	    }
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public IPage<BlackIpConfig> findBlackIpConfigs(QueryRequest request, BlackIpConfigQuery blackIpConfig) {
        LambdaQueryWrapper<BlackIpConfig> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(blackIpConfig.getBlackIp()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getBlackIp,blackIpConfig.getBlackIp());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getBlackIpFuzzy() ))
	    {
	    	queryWrapper.like(BlackIpConfig::getBlackIp,blackIpConfig.getBlackIpFuzzy());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getLimitProject()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getLimitProject,blackIpConfig.getLimitProject());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getIsavailability()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getIsavailability,blackIpConfig.getIsavailability());
	    }
	    if(CollectionUtils.isNotEmpty(blackIpConfig.getLimitProjectArray()))
	    {
	    	queryWrapper.in(BlackIpConfig::getLimitProject,blackIpConfig.getLimitProjectArray());
	    }
        if (blackIpConfig.getCreateStartDate() != null){//提交开始时间
        	queryWrapper.ge(BlackIpConfig::getCreateDate,blackIpConfig.getCreateStartDate());
		}
		if (blackIpConfig.getCreateEndDate() != null) {//提交结束时间
			queryWrapper.le(BlackIpConfig::getCreateDate, blackIpConfig.getCreateEndDate());
		}
		   //创建时间的排序方式
	   	if(blackIpConfig.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(blackIpConfig.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(BlackIpConfig::getCreateDate);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(BlackIpConfig::getCreateDate);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(BlackIpConfig::getCreateDate);
	   	}
        // TODO 设置查询条件
        Page<BlackIpConfig> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }
    
    @Override
    public List<BlackIpConfig> findBlackIpConfigs(BlackIpConfigQuery blackIpConfig) {
	    LambdaQueryWrapper<BlackIpConfig> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(blackIpConfig.getBlackIp()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getBlackIp,blackIpConfig.getBlackIp());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getBlackIpFuzzy() ))
	    {
	    	queryWrapper.like(BlackIpConfig::getBlackIp,blackIpConfig.getBlackIpFuzzy());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getLimitProject()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getLimitProject,blackIpConfig.getLimitProject());
	    }
	    if(StringUtils.isNotBlank(blackIpConfig.getIsavailability()))
	    {
	    	queryWrapper.eq(BlackIpConfig::getIsavailability,blackIpConfig.getIsavailability());
	    }
	    if(CollectionUtils.isNotEmpty(blackIpConfig.getLimitProjectArray()))
	    {
	    	queryWrapper.in(BlackIpConfig::getLimitProject,blackIpConfig.getLimitProjectArray());
	    }
        if (blackIpConfig.getCreateStartDate() != null){//提交开始时间
        	queryWrapper.ge(BlackIpConfig::getCreateDate,blackIpConfig.getCreateStartDate());
		}
		if (blackIpConfig.getCreateEndDate() != null) {//提交结束时间
			queryWrapper.le(BlackIpConfig::getCreateDate, blackIpConfig.getCreateEndDate());
		}
		   //创建时间的排序方式
	   	if(blackIpConfig.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(blackIpConfig.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(BlackIpConfig::getCreateDate);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(BlackIpConfig::getCreateDate);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(BlackIpConfig::getCreateDate);
	   	}
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }
    
    @Override
    @Transactional
    public void createBlackIpConfig(BlackIpConfig blackIpConfig) {
        this.save(blackIpConfig);
    }

    @Override
    @Transactional
    public void updateBlackIpConfig(BlackIpConfig blackIpConfig) {
        this.saveOrUpdate(blackIpConfig);
    }

    @Override
    @Transactional
    public void deleteBlackIpConfig(BlackIpConfig blackIpConfig) {
        LambdaQueryWrapper<BlackIpConfig> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteBlackIpConfigs(String[] blackIpConfigIds) {
        List<String> list = Arrays.asList(blackIpConfigIds);
        this.removeByIds(list);
    }
    
	@Override
	public void audit(String blackIpConfigIds, String isAvailability) {
		String[] ids = blackIpConfigIds.split(",");
		LambdaUpdateWrapper<BlackIpConfig> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(BlackIpConfig::getIsavailability, isAvailability);
		updateWrapper.in(BlackIpConfig::getId, ids);
		this.update(updateWrapper);
	}
}
