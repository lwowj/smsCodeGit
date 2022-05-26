package com.hero.sms.service.impl.agent;

import java.util.ArrayList;
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
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenuLimit;
import com.hero.sms.mapper.agent.AgentMenuLimitMapper;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.hero.sms.service.agent.IAgentService;

/**
 * 代理菜单关联表 Service实现
 *
 * @author Administrator
 * @date 2020-03-06 10:05:54
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentMenuLimitServiceImpl extends ServiceImpl<AgentMenuLimitMapper, AgentMenuLimit> implements IAgentMenuLimitService {

    @Autowired
    private AgentMenuLimitMapper agentMenuLimitMapper;
    @Autowired
    private IAgentService agentService;

    @Override
    public IPage<AgentMenuLimit> findAgentMenuLimits(QueryRequest request, AgentMenuLimit agentMenuLimit) {
        LambdaQueryWrapper<AgentMenuLimit> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<AgentMenuLimit> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentMenuLimit> findAgentMenuLimits(AgentMenuLimit agentMenuLimit) {
	    LambdaQueryWrapper<AgentMenuLimit> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        queryWrapper.eq(AgentMenuLimit::getAgentId,agentMenuLimit.getAgentId());
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据代理ID查询
     * @param agentId
     * @return
     */
    @Override
    public List<AgentMenuLimit> findAgentMenuLimitsByAgentId(int agentId) {
        LambdaQueryWrapper<AgentMenuLimit> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(AgentMenuLimit::getAgentId,agentId);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createAgentMenuLimit(AgentMenuLimit agentMenuLimit) {
        this.save(agentMenuLimit);
    }

    @Override
    @Transactional
    public void updateAgentMenuLimit(AgentMenuLimit agentMenuLimit) {
        this.saveOrUpdate(agentMenuLimit);
    }

    /**
     * 代理端修改权限
     * @param menuIds
     * @param agentId
     * @param upAgent
     */
    @Override
    @Transactional
    public void updateAgentMenuLimits(String menuIds, Long agentId, Agent upAgent) {
        Agent oldAgent = this.agentService.getById(agentId);
        if(!upAgent.getId().equals(oldAgent.getUpAgentId())){
            throw new FebsException("只能修改本代理的下级代理");
        }
        updateAgentMenuLimits(menuIds,agentId);
    }

    @Override
    @Transactional
    public void updateAgentMenuLimits(String menuIds,Long agentId) {
        LambdaQueryWrapper<AgentMenuLimit> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AgentMenuLimit::getAgentId,agentId);
        this.remove(deleteWrapper);
        if(StringUtils.isNotEmpty(menuIds)){
            String[] ids = menuIds.split(",");
            List<AgentMenuLimit> agentMenuLimits = new ArrayList<>();
            for (String id:ids){
                AgentMenuLimit agentMenuLimit = new AgentMenuLimit();
                agentMenuLimit.setAgentId(agentId);
                agentMenuLimit.setMenuId(Long.parseLong(id));
                agentMenuLimits.add(agentMenuLimit);
            }
            this.saveBatch(agentMenuLimits);
        }
    }

    @Override
    @Transactional
    public void deleteAgentMenuLimit(AgentMenuLimit agentMenuLimit) {
        LambdaQueryWrapper<AgentMenuLimit> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}
}
