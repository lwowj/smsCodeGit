package com.hero.sms.service.impl.agent;

import java.util.Arrays;
import java.util.Date;
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
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.entity.common.Code;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.mapper.agent.AgentSystemConfigMapper;
import com.hero.sms.service.agent.IAgentSystemConfigService;

/**
 * 代理系统配置 Service实现
 *
 * @author Administrator
 * @date 2020-03-18 12:10:37
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentSystemConfigServiceImpl extends ServiceImpl<AgentSystemConfigMapper, AgentSystemConfig> implements IAgentSystemConfigService {

    @Autowired
    private AgentSystemConfigMapper agentSystemConfigMapper;

    @Override
    public IPage<AgentSystemConfig> findAgentSystemConfigs(QueryRequest request, AgentSystemConfig agentSystemConfig) {
        LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(agentSystemConfig.getAgentId() != null){
            queryWrapper.eq(AgentSystemConfig::getAgentId,agentSystemConfig.getAgentId());
        }
        if(StringUtils.isNotEmpty(agentSystemConfig.getSystemName())){
            queryWrapper.like(AgentSystemConfig::getSystemName,agentSystemConfig.getSystemName());
        }
        if(StringUtils.isNotEmpty(agentSystemConfig.getSystemUrl())){
            queryWrapper.like(AgentSystemConfig::getSystemUrl,agentSystemConfig.getSystemUrl());
        }
        if(StringUtils.isNotEmpty(agentSystemConfig.getApproveStateCode())){
            queryWrapper.eq(AgentSystemConfig::getApproveStateCode,agentSystemConfig.getApproveStateCode());
        }
        queryWrapper.orderByDesc(AgentSystemConfig::getId);
        Page<AgentSystemConfig> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentSystemConfig> findAgentSystemConfigs(AgentSystemConfig agentSystemConfig) {
	    LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据代理ID获取
     * @param agentSystemConfig
     * @param agentId
     * @return
     */
    @Override
    public AgentSystemConfig findAgentSystemConfig(AgentSystemConfig agentSystemConfig, Integer agentId) {
	    LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        queryWrapper.eq(AgentSystemConfig::getAgentId,agentId);
        AgentSystemConfig systemConfig = this.baseMapper.selectOne(queryWrapper);
		return systemConfig;
    }

    @Override
    @Transactional
    public void createAgentSystemConfig(AgentSystemConfig agentSystemConfig) {
        this.save(agentSystemConfig);
    }

    /**
     * 代理端新增
     * @param agentSystemConfig
     * @param agent
     */
    @Override
    @Transactional
    public void createAgentSystemConfig(AgentSystemConfig agentSystemConfig, Agent agent) {
        try {
            if(agentSystemConfig == null || StringUtils.isEmpty(agentSystemConfig.getSystemName())
                    || agentSystemConfig.getLogoFile() == null || StringUtils.isEmpty(agentSystemConfig.getSystemUrl())){
                throw new FebsException("设置的数据不完整！");
            }
            LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AgentSystemConfig::getSystemUrl,agentSystemConfig.getSystemUrl());
            queryWrapper.ne(AgentSystemConfig::getAgentId,agent.getId());
            List<AgentSystemConfig> list = this.list(queryWrapper);
            if(list.size() != 0 ){
                throw new FebsException("该域名已有，请更换域名重新绑定！");
            }
            LambdaQueryWrapper<AgentSystemConfig> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(AgentSystemConfig::getAgentId,agent.getId());
            this.remove(deleteWrapper);
            if(agentSystemConfig.getLogoFile() != null){
                Code path = DatabaseCache.getCodeBySortCodeAndCode("System","logoFilePath");
                Code size = DatabaseCache.getCodeBySortCodeAndCode("System","pictureSize");
                StringBuffer imgPath = new StringBuffer();
                imgPath.append("/agent/").append(agent.getAgentAccount()).append(".png");
                FileUtil.savePpictureFile(agentSystemConfig.getLogoFile(),path.getName() + imgPath.toString(),Long.parseLong(size.getName()));
                agentSystemConfig.setSystemLogoUrl("/res" + imgPath.toString());
            }
            agentSystemConfig.setCreateTime(new Date());
            agentSystemConfig.setApproveStateCode(OrgApproveStateEnums.UNVERIFIED.getCode());
            this.save(agentSystemConfig);
        }catch (Exception e){
            throw new FebsException(e.getMessage());
        }
    }


    @Override
    @Transactional
    public void updateAgentSystemConfig(AgentSystemConfig agentSystemConfig) {
        this.saveOrUpdate(agentSystemConfig);
    }

    @Override
    @Transactional
    public void deleteAgentSystemConfig(AgentSystemConfig agentSystemConfig) {
        LambdaQueryWrapper<AgentSystemConfig> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteAgentSystemConfigs(String[] agentSystemConfigIds) {
        List<String> list = Arrays.asList(agentSystemConfigIds);
        this.removeByIds(list);
    }

    /**
     * 批量认证代理配置
     * @param configIds
     * @param approveState
     */
    @Override
    public void approveAgentConfigs(String[] configIds,String approveState){
        List<String> list = Arrays.asList(configIds);
        LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AgentSystemConfig::getId,list);
        AgentSystemConfig organization = new AgentSystemConfig();
        organization.setApproveStateCode(approveState);
        this.update(organization,queryWrapper);
    }
}
