package com.hero.sms.service.organization;


import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationLog;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;

/**
 * 商户操作日志表 Service接口
 *
 * @author Administrator
 * @date 2020-03-21 23:29:41
 */
public interface IOrganizationLogService extends IService<OrganizationLog> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationLog organizationLog
     * @return IPage<OrganizationLog>
     */
    IPage<OrganizationLog> findOrganizationLogs(QueryRequest request, OrganizationLog organizationLog, Date organizationLogStartTime, Date organizationLogEndTime);

    /**
     * 查询（所有）
     *
     * @param organizationLog organizationLog
     * @return List<OrganizationLog>
     */
    List<OrganizationLog> findOrganizationLogs(OrganizationLog organizationLog);

    /**
     * 新增
     *
     * @param organizationLog organizationLog
     */
    void createOrganizationLog(OrganizationLog organizationLog);

    /**
     * 修改
     *
     * @param organizationLog organizationLog
     */
    void updateOrganizationLog(OrganizationLog organizationLog);

    /**
     * 删除
     *
     * @param organizationLog organizationLog
     */
    void deleteOrganizationLog(OrganizationLog organizationLog);

    /**
    * 删除
    *
    * @param organizationLogIds organizationLogIds
    */
    void deleteOrganizationLogs(String[] organizationLogIds);

    /**
     * 保存商户用户操作日志
     * @param point
     * @param method
     * @param ip
     * @param operation
     * @param start
     */
    void saveLog(ProceedingJoinPoint point, Method method, String ip, String operation, long start, OrganizationUserExt user);
}
