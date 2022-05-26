package com.hero.sms.service.impl.organization;

import com.google.common.collect.Lists;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.organization.OrganizationGroup;
import com.hero.sms.mapper.organization.OrganizationGroupMapper;
import com.hero.sms.service.organization.IOrganizationGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Arrays;

/**
 * 商户分组表 Service实现
 *
 * @author Administrator
 * @date 2020-06-20 22:38:28
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationGroupServiceImpl extends ServiceImpl<OrganizationGroupMapper, OrganizationGroup> implements IOrganizationGroupService {

    @Autowired
    private OrganizationGroupMapper organizationGroupMapper;

    @Override
    public IPage<OrganizationGroup> findOrganizationGroups(QueryRequest request, OrganizationGroup organizationGroup) {
        LambdaQueryWrapper<OrganizationGroup> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<OrganizationGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationGroup> findOrganizationGroups(OrganizationGroup organizationGroup) {
	    LambdaQueryWrapper<OrganizationGroup> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationGroup(OrganizationGroup organizationGroup) {
        this.save(organizationGroup);
    }

    @Override
    @Transactional
    public void updateOrganizationGroup(OrganizationGroup organizationGroup) {
        this.saveOrUpdate(organizationGroup);
    }

    @Override
    @Transactional
    public void deleteOrganizationGroup(OrganizationGroup organizationGroup) {
        LambdaQueryWrapper<OrganizationGroup> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationGroups(String[] organizationGroupIds) {
        List<String> list = Arrays.asList(organizationGroupIds);
        this.removeByIds(list);
    }

    @Override
    public void incrementSaveOrganizationGroup(String orgCode, String groupIds) throws ServiceException {
        if (StringUtils.isBlank(orgCode)){
            throw new ServiceException("商户编码不能为空！");
        }
        //商户分组重置清空
        LambdaQueryWrapper<OrganizationGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationGroup::getOrgCode,orgCode);
        this.remove(wrapper);

        if (StringUtils.isBlank(groupIds)){
            return;
        }
        //商户分组重置保存
        List<OrganizationGroup> list = Lists.newArrayList();
        Arrays.asList(groupIds.split(",")).stream().forEach( item -> {
            OrganizationGroup group = new OrganizationGroup();
            group.setOrgCode(orgCode);
            group.setGroupId(item);
            group.setCreateTime(new Date());
            list.add(group);
        });

        this.saveBatch(list);

    }
}
