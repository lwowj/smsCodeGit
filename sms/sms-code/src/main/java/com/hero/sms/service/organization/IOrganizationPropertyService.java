package com.hero.sms.service.organization;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationProperty;

/**
 * 商户属性 Service接口
 *
 * @author Administrator
 * @date 2020-05-01 19:49:03
 */
public interface IOrganizationPropertyService extends IService<OrganizationProperty> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationProperty organizationProperty
     * @return IPage<OrganizationProperty>
     */
    IPage<OrganizationProperty> findOrganizationPropertys(QueryRequest request, OrganizationProperty organizationProperty);

    /**
     * 查询（所有）
     *
     * @param organizationProperty organizationProperty
     * @return List<OrganizationProperty>
     */
    List<OrganizationProperty> findOrganizationPropertys(OrganizationProperty organizationProperty);

    /**
     * 新增
     *
     * @param organizationProperty organizationProperty
     */
    void createOrganizationProperty(OrganizationProperty organizationProperty);

    @Transactional
    void createDefaultSmppProperty(String orgCode, String remark);

    /**
     * 修改
     *
     * @param organizationProperty organizationProperty
     */
    void updateOrganizationProperty(OrganizationProperty organizationProperty);

    /**
     * 删除
     *
     * @param organizationProperty organizationProperty
     */
    void deleteOrganizationProperty(OrganizationProperty organizationProperty);

    /**
    * 删除
    *
    * @param organizationPropertyIds organizationPropertyIds
    */
    void deleteOrganizationPropertys(String[] organizationPropertyIds);

    void updateSmsChannelProperties(List<OrganizationProperty> organizationProperties, String username);
}
