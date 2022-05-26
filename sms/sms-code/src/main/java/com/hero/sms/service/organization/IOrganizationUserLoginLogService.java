package com.hero.sms.service.organization;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationUserLoginLog;
import com.hero.sms.entity.organization.OrganizationUserLoginLogQuery;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;

/**
 * 商户用户登录日志表 Service接口
 *
 * @author Administrator
 * @date 2020-12-25 17:33:01
 */
public interface IOrganizationUserLoginLogService extends IService<OrganizationUserLoginLog> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationUserLoginLog organizationUserLoginLog
     * @return IPage<OrganizationUserLoginLog>
     */
    IPage<OrganizationUserLoginLog> findOrganizationUserLoginLogs(QueryRequest request, OrganizationUserLoginLog organizationUserLoginLog);

    /**
     * 查询（所有）
     *
     * @param organizationUserLoginLog organizationUserLoginLog
     * @return List<OrganizationUserLoginLog>
     */
    List<OrganizationUserLoginLog> findOrganizationUserLoginLogs(OrganizationUserLoginLog organizationUserLoginLog);

    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationUserLoginLog organizationUserLoginLog
     * @return IPage<OrganizationUserLoginLog>
     */
    IPage<OrganizationUserLoginLog> findOrganizationUserLoginLogs(QueryRequest request, OrganizationUserLoginLogQuery organizationUserLoginLog);

    /**
     * 查询（所有）
     *
     * @param organizationUserLoginLog organizationUserLoginLog
     * @return List<OrganizationUserLoginLog>
     */
    List<OrganizationUserLoginLog> findOrganizationUserLoginLogs(OrganizationUserLoginLogQuery organizationUserLoginLog);
    
    /**
     * 新增
     *
     * @param organizationUserLoginLog organizationUserLoginLog
     */
    void createOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog);

    /**
     * 修改
     *
     * @param organizationUserLoginLog organizationUserLoginLog
     */
    void updateOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog);

    /**
     * 删除
     *
     * @param organizationUserLoginLog organizationUserLoginLog
     */
    void deleteOrganizationUserLoginLog(OrganizationUserLoginLog organizationUserLoginLog);

    /**
    * 删除
    *
    * @param organizationUserLoginLogIds organizationUserLoginLogIds
    */
    void deleteOrganizationUserLoginLogs(String[] organizationUserLoginLogIds);
    
    /**
     * 保存商户用户登录日志
     * @param start
     */
    void saveLog(long start, OrganizationUserLoginLog organizationUserLoginLog);
}
