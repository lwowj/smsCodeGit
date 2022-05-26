package com.hero.sms.service.impl.organization;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.mapper.organization.OrganizationUserLimitMapper;
import com.hero.sms.mapper.organization.OrganizationUserMapper;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商户用户菜单关联表 Service实现
 *
 * @author Administrator
 * @date 2020-03-08 00:13:33
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationUserLimitServiceImpl extends ServiceImpl<OrganizationUserLimitMapper, OrganizationUserLimit> implements IOrganizationUserLimitService {

    @Autowired
    private OrganizationUserLimitMapper organizationUserLimitMapper;
    @Autowired
    private OrganizationUserMapper organizationUserMapper;

    @Override
    public IPage<OrganizationUserLimit> findOrganizationUserLimits(QueryRequest request, OrganizationUserLimit organizationUserLimit) {
        LambdaQueryWrapper<OrganizationUserLimit> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<OrganizationUserLimit> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationUserLimit> findOrganizationUserLimits(OrganizationUserLimit organizationUserLimit) {
	    LambdaQueryWrapper<OrganizationUserLimit> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if(organizationUserLimit.getUserId()!=null){
            queryWrapper.eq(OrganizationUserLimit::getUserId,organizationUserLimit.getUserId());
        }
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationUserLimit(OrganizationUserLimit organizationUserLimit) {
        this.save(organizationUserLimit);
    }

    @Override
    @Transactional
    public void updateOrganizationUserLimit(OrganizationUserLimit organizationUserLimit) {
        this.saveOrUpdate(organizationUserLimit);
    }

    @Override
    @Transactional
    public void deleteOrganizationUserLimit(OrganizationUserLimit organizationUserLimit) {
        LambdaQueryWrapper<OrganizationUserLimit> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationUserLimits(String[] organizationUserLimitIds) {
        List<String> list = Arrays.asList(organizationUserLimitIds);
        this.removeByIds(list);
    }

    /**
     * 商户端批量更新
     * @param menuIds
     * @param userId
     */
    @Override
    @Transactional
    public void updateeOrganizationUserLimits(String menuIds, Long userId, OrganizationUserExt userExt) {
        LambdaQueryWrapper<OrganizationUser> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(OrganizationUser::getOrganizationCode,userExt.getOrganizationCode());
        queryWrapper.eq(OrganizationUser::getId,userId);
        queryWrapper.ne(OrganizationUser::getAccountType,DatabaseCache.getCodeBySortCodeAndName("AccountType","主账户").getCode());
        OrganizationUser user = organizationUserMapper.selectOne(queryWrapper);
        if(user == null){
            throw new FebsException("无权修改该商户");
        }
        updateeOrganizationUserLimits(menuIds,userId);
    }

    /**
     * 代理端批量更新
     * @param menuIds
     * @param userId
     */
    @Override
    @Transactional
    public void updateeOrganizationUserLimits(String menuIds, Long userId, Agent agent) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("u.id",userId);
        queryWrapper.eq("o.Agent_Id",agent.getId());
//        queryWrapper.ne("u.`Account_Type`",DatabaseCache.getCodeBySortCodeAndName("AccountType","主账户").getCode());
        List<OrganizationUser> users = organizationUserMapper.selectLeftOrganization(queryWrapper);
        if(users.size() < 1){
            throw new FebsException("无权修改该商户");
        }
        updateeOrganizationUserLimits(menuIds,userId);
    }

    /**
     * 批量更新
     * @param menuIds
     * @param userId
     */
    @Override
    @Transactional
    public void updateeOrganizationUserLimits(String menuIds,Long userId) {
        LambdaQueryWrapper<OrganizationUserLimit> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(OrganizationUserLimit::getUserId,userId);
        this.remove(deleteWrapper);
        if(StringUtils.isNotEmpty(menuIds)){
            String[] ids = menuIds.split(",");
            List<OrganizationUserLimit> agentMenuLimits = new ArrayList<>();
            for (String id:ids){
                OrganizationUserLimit menuLimit = new OrganizationUserLimit();
                menuLimit.setUserId(userId);
                menuLimit.setMenuId(Long.parseLong(id));
                agentMenuLimits.add(menuLimit);
            }
            this.saveBatch(agentMenuLimits);
        }
    }

    @Override
    public List<OrganizationUserLimit> findOrgMenuLimitsByUserId(int userId) {
        LambdaQueryWrapper<OrganizationUserLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationUserLimit::getUserId,userId);
        return this.baseMapper.selectList(wrapper);
    }
}
