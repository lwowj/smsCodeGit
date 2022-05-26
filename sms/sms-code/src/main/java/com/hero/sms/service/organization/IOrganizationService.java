package com.hero.sms.service.organization;

import java.util.List;
import java.util.Map;

import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.organization.OrganizationQuery;
import com.hero.sms.entity.organization.ext.OrganizationExtGroup;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.NotifyMsgStateModel;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.ext.OrganizationExt;


/**
 * 商户信息 Service接口
 *
 * @author Administrator
 * @date 2020-03-07 17:24:55
 */
public interface IOrganizationService extends IService<Organization> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organization organization
     * @return IPage<Organization>
     */
    IPage<Organization> findOrganizations(QueryRequest request, Organization organization);

    /**
     * 分页查询 关联商户分组
     * @param request
     * @param organization
     * @return
     */
    IPage<OrganizationExtGroup> findOrganizationExtGroups(QueryRequest request, OrganizationQuery organization);

    /**
     * 查询（所有）
     *
     * @param organization organization
     * @return List<Organization>
     */
    List<Organization> findOrganizations(Organization organization);

    /**
     * 根据商户编号查询
     *
     * @param orgCode
     * @return Organization
     */
    Organization getOrganizationByCode(String orgCode);

    /**
     * 新增
     *
     * @param organization organization
     */
    void createOrganization(Organization organization,String userAccount,String userPassword);
    
    void createOrganization(Organization organization,String userAccount,String userPassword,int needBindGoogleKey);

    /**
     * 校验DATA-MD5
     * @param organization
     * @return
     */
    boolean checkDataMd5(Organization organization);

    /**
     * 修改
     *
     * @param organization organization
     */
    void updateOrganization(Organization organization);

    /**
     * 更新商户判断代理
     * @param organization
     * @param agentId
     */
    @Transactional
    void updateOrganization(Organization organization, int agentId);

    /**
     * 删除
     *
     * @param organization organization
     */
    void deleteOrganization(Organization organization);

    /**
    * 删除
    *
    * @param organizationIds organizationIds
    */
    void deleteOrganizations(String[] organizationIds);

    /**
     * 批量认证
     * @param organizationIds
     */
    @Transactional
    void approveOrgs(String[] organizationIds,String approveState);

    public Organization queryOrgByCodeForUpdate(String code);

    /**
     * 通过orgCode级联查询商户及资费信息
     * @param orgCode
     * @return
     */
    public OrganizationExt queryOrganizationExtByOrgCode(String orgCode);

    /**
     * 获取DATAMD5
     * @param organization
     * @return
     */
    public String getDataMd5(Organization organization);

    public Organization queryOrgByCode(String organizationCode);


    void cancelOrganization(String organizationCode,String orgAmountHandle,String currentUserName);

    /**
     * 获取商户缓存
     * @return
     */
    public List<Map<String,String>> getOrgs();
    /**
     * 根据代理ID获取商户缓存
     * @return
     */
    List<Map<String,String>> getOrgsByAgentId(Integer id);

    public void notifyMsgState(Organization org, NotifyMsgStateModel model) throws Exception;

    /**
     * 检查域名是否符合配置
     * @param domainName
     * @param userAccount
     */
    void checkDomainName(String domainName, String userAccount);

    public List<OrganizationExt> findListContainProperty(Organization organization);

    /**
     * 检查余额
     * @param orgCode   商户编号
     * @param smsType   短信类型
     * @param costName  短信资费
     * @param smsCount    短信数量
     * @return
     */
    boolean checkOrgAmountPrePay(String orgCode, Integer smsType, String costName, int smsCount) throws ServiceException;
    
    /**
     * @begin 2021-11-12
     * 统计商户信息数据
     * @param organization
     * @return
     */
    public Map<String, Object> statisticOrganizationInfo(OrganizationQuery organization);
}
