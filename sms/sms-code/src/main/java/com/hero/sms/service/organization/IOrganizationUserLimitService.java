package com.hero.sms.service.organization;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商户用户菜单关联表 Service接口
 *
 * @author Administrator
 * @date 2020-03-08 00:13:33
 */
public interface IOrganizationUserLimitService extends IService<OrganizationUserLimit> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationUserLimit organizationUserLimit
     * @return IPage<OrganizationUserLimit>
     */
    IPage<OrganizationUserLimit> findOrganizationUserLimits(QueryRequest request, OrganizationUserLimit organizationUserLimit);

    /**
     * 查询（所有）
     *
     * @param organizationUserLimit organizationUserLimit
     * @return List<OrganizationUserLimit>
     */
    List<OrganizationUserLimit> findOrganizationUserLimits(OrganizationUserLimit organizationUserLimit);

    /**
     * 新增
     *
     * @param organizationUserLimit organizationUserLimit
     */
    void createOrganizationUserLimit(OrganizationUserLimit organizationUserLimit);

    /**
     * 修改
     *
     * @param organizationUserLimit organizationUserLimit
     */
    void updateOrganizationUserLimit(OrganizationUserLimit organizationUserLimit);

    /**
     * 删除
     *
     * @param organizationUserLimit organizationUserLimit
     */
    void deleteOrganizationUserLimit(OrganizationUserLimit organizationUserLimit);

    /**
    * 删除
    *
    * @param organizationUserLimitIds organizationUserLimitIds
    */
    void deleteOrganizationUserLimits(String[] organizationUserLimitIds);

    /**
     * 商户端批量更新
     * @param menuIds
     * @param userId
     */
    @Transactional
    void updateeOrganizationUserLimits(String menuIds, Long userId, OrganizationUserExt userExt);

    /**
     * 代理端批量更新
     * @param menuIds
     * @param userId
     */
    @Transactional
    void updateeOrganizationUserLimits(String menuIds, Long userId, Agent agent);

    /**
     * 批量更新
     * @param menuIds
     * @param userId
     */
    @Transactional
    void updateeOrganizationUserLimits(String menuIds, Long userId);

    /**
     * 根据商户用户ID查询
     * @param userId
     * @return
     */
    List<OrganizationUserLimit> findOrgMenuLimitsByUserId(int userId);
}
