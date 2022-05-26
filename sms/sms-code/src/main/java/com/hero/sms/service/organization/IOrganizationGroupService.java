package com.hero.sms.service.organization;

import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.organization.OrganizationGroup;

import com.hero.sms.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商户分组表 Service接口
 *
 * @author Administrator
 * @date 2020-06-20 22:38:28
 */
public interface IOrganizationGroupService extends IService<OrganizationGroup> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationGroup organizationGroup
     * @return IPage<OrganizationGroup>
     */
    IPage<OrganizationGroup> findOrganizationGroups(QueryRequest request, OrganizationGroup organizationGroup);

    /**
     * 查询（所有）
     *
     * @param organizationGroup organizationGroup
     * @return List<OrganizationGroup>
     */
    List<OrganizationGroup> findOrganizationGroups(OrganizationGroup organizationGroup);

    /**
     * 新增
     *
     * @param organizationGroup organizationGroup
     */
    void createOrganizationGroup(OrganizationGroup organizationGroup);

    /**
     * 修改
     *
     * @param organizationGroup organizationGroup
     */
    void updateOrganizationGroup(OrganizationGroup organizationGroup);

    /**
     * 删除
     *
     * @param organizationGroup organizationGroup
     */
    void deleteOrganizationGroup(OrganizationGroup organizationGroup);

    /**
    * 删除
    *
    * @param organizationGroupIds organizationGroupIds
    */
    void deleteOrganizationGroups(String[] organizationGroupIds);

    void incrementSaveOrganizationGroup(String orgCode, String groupIds) throws ServiceException;
}
