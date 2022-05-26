package com.hero.sms.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalAgent;
import com.hero.sms.entity.message.StatisticalAgentQuery;
import com.hero.sms.mapper.message.StatisticalAgentMapper;
import com.hero.sms.service.message.IStatisticalAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 按照代理统计 Service实现
 *
 * @author Administrator
 * @date 2020-03-20 14:55:46
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class StatisticalAgentServiceImpl extends ServiceImpl<StatisticalAgentMapper, StatisticalAgent> implements IStatisticalAgentService {

    @Autowired
    private StatisticalAgentMapper statisticalAgentMapper;

    @Override
    public IPage<StatisticalAgent> findStatisticalAgents(QueryRequest request, StatisticalAgentQuery statisticalAgent) {
        LambdaQueryWrapper<StatisticalAgent> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (statisticalAgent.getAgentId() != null){
            queryWrapper.eq(StatisticalAgent::getAgentId,statisticalAgent.getAgentId());
        }
        if (statisticalAgent.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalAgent::getStatisticalDate,statisticalAgent.getStatisticalStartTime());
        }
        if (statisticalAgent.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalAgent::getStatisticalDate,statisticalAgent.getStatisticalEndTime());
        }
        queryWrapper.orderByDesc(StatisticalAgent::getStatisticalDate);
        queryWrapper.orderByDesc(StatisticalAgent::getTotalCount);
        Page<StatisticalAgent> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<StatisticalAgent> findStatisticalAgents(StatisticalAgentQuery statisticalAgent) {
	    LambdaQueryWrapper<StatisticalAgent> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (statisticalAgent.getAgentId() != null){
            queryWrapper.eq(StatisticalAgent::getAgentId,statisticalAgent.getAgentId());
        }
        if (statisticalAgent.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalAgent::getStatisticalDate,statisticalAgent.getStatisticalStartTime());
        }
        if (statisticalAgent.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalAgent::getStatisticalDate,statisticalAgent.getStatisticalEndTime());
        }
        queryWrapper.orderByDesc(StatisticalAgent::getStatisticalDate);
        queryWrapper.orderByDesc(StatisticalAgent::getTotalCount);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createStatisticalAgent(StatisticalAgent statisticalAgent) {
        this.save(statisticalAgent);
    }

    @Override
    @Transactional
    public void updateStatisticalAgent(StatisticalAgentQuery statisticalAgent) {
        this.saveOrUpdate(statisticalAgent);
    }

    @Override
    @Transactional
    public void deleteStatisticalAgent(StatisticalAgentQuery statisticalAgent) {
        LambdaQueryWrapper<StatisticalAgent> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteStatisticalAgents(String[] statisticalAgentIds) {
        List<String> list = Arrays.asList(statisticalAgentIds);
        this.removeByIds(list);
    }
}
