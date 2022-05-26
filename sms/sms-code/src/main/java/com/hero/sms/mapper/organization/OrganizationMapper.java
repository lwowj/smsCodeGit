package com.hero.sms.mapper.organization;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationQuery;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.entity.organization.ext.OrganizationExtGroup;


/**
 * 商户信息 Mapper
 *
 * @author Administrator
 * @date 2020-03-07 17:24:55
 */
public interface OrganizationMapper extends BaseMapper<Organization> {

    OrganizationExt queryOrganizationExtByOrgCode(String orgCode);

    Organization queryOrganizationByUserAccount(String userAccount);

    void deleteOrganizationUserByOrganization(@Param(Constants.WRAPPER) Wrapper wrapper);

	OrganizationExt findContainPropertyById(Integer id);

	OrganizationExt findContainPropertyByCode(String organizationCode);

	List<OrganizationExt> findListContainProperty(Organization organization);

    IPage<OrganizationExtGroup> findListContainsGroup(Page<OrganizationExtGroup> page, @Param("oq") OrganizationQuery organizationQuery);
    
    Map<String, Object> statisticOrganizationInfo(@Param("ew") LambdaQueryWrapper<Organization> ew);
}
