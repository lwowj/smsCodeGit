package com.hero.sms.service.organization;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.organization.OrganizationCost;

/**
 * 商户用户资费 Service接口
 *
 * @author Administrator
 * @date 2020-03-08 00:12:30
 */
public interface IOrganizationCostService extends IService<OrganizationCost> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationCost organizationCost
     * @return IPage<OrganizationCost>
     */
    IPage<OrganizationCost> findOrganizationCosts(QueryRequest request, OrganizationCost organizationCost);

    /**
     * 查询（所有）
     *
     * @param organizationCost organizationCost
     * @return List<OrganizationCost>
     */
    List<OrganizationCost> findOrganizationCosts(OrganizationCost organizationCost);

    /**
     * 新增
     *
     * @param organizationCost organizationCost
     */
    void createOrganizationCost(OrganizationCost organizationCost);

    /**
     * 修改
     *
     * @param organizationCost organizationCost
     */
    void updateOrganizationCost(OrganizationCost organizationCost);

    /**
     * 删除
     *
     * @param organizationCost organizationCost
     */
    void deleteOrganizationCost(OrganizationCost organizationCost);

    /**
    * 删除
    *
    * @param organizationCostIds organizationCostIds
    */
    void deleteOrganizationCosts(String[] organizationCostIds);

    /**
     * 批量修改
     * @param costs
     * @param userName
     */
    @Transactional
    void updateOrganizationCosts(List<OrganizationCost> costs, String userName);

    /**
     * 代理端修改费率
     * @param costs
     * @param agent
     */
    @Transactional
    void updateOrganizationCostsOnAgent(List<OrganizationCost> costs, Agent agent);

    /**
     * 指定通道
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param state
     * @param channelId
     */
	void orgAssignChannel(String organizationCodes,String costName,String smsType,String state,String channelId);

    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     */
    void updateCosts(String organizationCodes, String costName, String smsType, String costValue);
    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     */
    void updateCosts(String organizationCodes, String costName, String smsType, String costValue,String operator,String userName);
    /**
     * 批量新增资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     */
    void addOrgCosts(String organizationCodes, String costName,String smsType, String costValue,String operator,String userName);
    /**
     * 批量新增资费
     * @param costs
     */
    void addOrgCosts(List<OrganizationCost> costs);
    
    /**
     * 批量浮动更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     * @param updateType
     */
    void updateFloatCosts(String organizationCodes, String costName,String smsType, String costValue,String operator,String userName,String updateType);
    
    /**
     * 筛选商户已配置的地区
     * @param orgCode
     * @return
     */
    List<AreaCode> getOrgAreaCodeList(String orgCode);
}
