package com.hero.sms.service.impl.organization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.mapper.organization.OrganizationCostMapper;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.utils.StringUtil;

/**
 * 商户用户资费 Service实现
 *
 * @author Administrator
 * @date 2020-03-08 00:12:30
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationCostServiceImpl extends ServiceImpl<OrganizationCostMapper, OrganizationCost> implements IOrganizationCostService {

    @Autowired
    private OrganizationCostMapper organizationCostMapper;
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IAgentCostService agentCostService;

    @Override
    public IPage<OrganizationCost> findOrganizationCosts(QueryRequest request, OrganizationCost organizationCost) {
        LambdaQueryWrapper<OrganizationCost> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<OrganizationCost> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationCost> findOrganizationCosts(OrganizationCost organizationCost) {
	    LambdaQueryWrapper<OrganizationCost> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        queryWrapper.eq(OrganizationCost::getOrganizationCode,organizationCost.getOrganizationCode());
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationCost(OrganizationCost organizationCost) {
        this.save(organizationCost);
    }

    @Override
    @Transactional
    public void updateOrganizationCost(OrganizationCost organizationCost) {
        this.saveOrUpdate(organizationCost);
    }

    @Override
    @Transactional
    public void deleteOrganizationCost(OrganizationCost organizationCost) {
        LambdaQueryWrapper<OrganizationCost> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationCosts(String[] organizationCostIds) {
        List<String> list = Arrays.asList(organizationCostIds);
        this.removeByIds(list);
    }

    @Override
    @Transactional
    public void updateOrganizationCosts(List<OrganizationCost> costs, String userName) {
        if(costs.size() > 0){
            LambdaQueryWrapper<OrganizationCost> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(OrganizationCost::getOrganizationCode,costs.get(0).getOrganizationCode());
            this.remove(deleteWrapper);
            Organization organization = organizationService.getOrganizationByCode(costs.get(0).getOrganizationCode());
            if(OrgStatusEnums.Cancel.getCode().equals(organization.getStatus())){
                throw new FebsException("该商户已经作废，无法操作！");
            }
            LambdaQueryWrapper<AgentCost> agentWrapper = new LambdaQueryWrapper<>();
            agentWrapper.eq(AgentCost::getAgentId,organization.getAgentId());
            List<AgentCost> agentCosts = this.agentCostService.list(agentWrapper);
            //key 新增运营商标识 StringUtil.isBlank(a.getOperator())?"":a.getOperator()
            Map<String,String> agentMap = agentCosts.stream().collect(Collectors.toMap(a -> a.getName() + a.getSmsType()+ (StringUtil.isBlank(a.getOperator())?"":a.getOperator()), a -> a.getValue()));
            Map costMap = new HashMap();
            Map<String,String> chinaMap = new HashMap<String,String>();
            Iterator<OrganizationCost> it = costs.iterator();
            while (it.hasNext()) {
                OrganizationCost cost = it.next();
                if (StringUtils.isEmpty(cost.getCostName()) ||StringUtils.isBlank(cost.getCostValue()) || cost.getSmsType() == null || StringUtils.isEmpty(cost.getState())) {
                    it.remove();
                }else {
                	//新增区域判断。非国内不设置运营商
                	if(!cost.getCostName().equals(SmsNumberAreaCodeEnums.China.getInArea()))
                	{
                		cost.setOperator(null);
                	}
                	else 
                	{
                		if(StringUtil.isBlank(cost.getOperator()))
                		{
                			chinaMap.put(SmsNumberAreaCodeEnums.China.getInArea(), SmsNumberAreaCodeEnums.China.getInArea());
                		}
                		else
                		{
                			chinaMap.put(cost.getOperator(), cost.getOperator());
                		}
                	}
                    String agentCost = agentMap.get(cost.getCostName()+cost.getSmsType()+ (StringUtil.isBlank(cost.getOperator())?"":cost.getOperator()));
                    if(StringUtils.isEmpty(agentCost)){
                        throw new FebsException("该资费类型代理未配置！");
                    }
                    if(Integer.parseInt(agentCost) > Integer.parseInt(cost.getCostValue())){
                        throw new FebsException("该资费类型配置不合理,请检查所属代理同类型配置！！");
                    }
                    //新增资费不为负数判断
                    if (Integer.parseInt(cost.getCostValue()) < 0) {
                    	throw new FebsException("该资费类型配置不合理,资费不能为负！");
                    }
                    costMap.put(cost.getCostName()+cost.getSmsType()+(StringUtil.isBlank(cost.getOperator())?"":cost.getOperator()),cost.getCostValue());
                    cost.setDescription("");
                    cost.setRemark(userName + "添加");
                    cost.setCreateDate(new Date());
                }
            }
            if(costs.size()!=costMap.size()) {
                throw new FebsException("相同的资费类型只能添加一条！");
            }
            if(chinaMap != null && !chinaMap.isEmpty()) 
            {
            	if(chinaMap.get(SmsNumberAreaCodeEnums.China.getInArea()) == null)
            	{
            		 throw new FebsException("国内资费必须添加一条运营商开放的配置！");
            	}
            }
            this.saveBatch(costs);
        }
    }

    /**
     * 代理端修改费率
     * @param costs
     * @param agent
     */
    @Override
    @Transactional
    public void updateOrganizationCostsOnAgent(List<OrganizationCost> costs, Agent agent) {
        if(costs.size() > 0){
            Organization organization =organizationService.getOrganizationByCode(costs.get(0).getOrganizationCode());
            if(!organization.getAgentId().equals(agent.getId())){
                throw new FebsException("不属于当前代理的商户无法修改");
            }
            updateOrganizationCosts(costs,agent.getAgentAccount());
        }
    }

	@Override
	public void orgAssignChannel(String organizationCodes, String costName,String smsType, String state, String channelId) {
		LambdaUpdateWrapper<OrganizationCost> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(OrganizationCost::getState, state);
		updateWrapper.set(OrganizationCost::getChannelId, channelId);
		updateWrapper.in(OrganizationCost::getOrganizationCode, organizationCodes.split(","));
		updateWrapper.eq(OrganizationCost::getCostName, costName);
		updateWrapper.eq(OrganizationCost::getSmsType, smsType);
		update(updateWrapper);
	}

    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     */
    @Override
    public void updateCosts(String organizationCodes, String costName,String smsType, String costValue) {
        updateCosts(organizationCodes, costName, smsType, costValue, null,null);
    }
    
    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     */
    @Override
    public void updateCosts(String organizationCodes, String costName,String smsType, String costValue,String operator,String userName) 
    {
    	String[] organizationCode = organizationCodes.split(",");
        LambdaUpdateWrapper<OrganizationCost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(OrganizationCost::getCostValue, costValue);
        updateWrapper.in(OrganizationCost::getOrganizationCode, organizationCode);
        updateWrapper.eq(OrganizationCost::getCostName, costName);
        updateWrapper.eq(OrganizationCost::getSmsType, smsType);
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		updateWrapper.eq(OrganizationCost::getOperator, operator);
        	}
        	else
        	{
        		updateWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(OrganizationCost::getOperator)
    	    			.or().eq(OrganizationCost::getOperator, "")
    	    			);
        	}
        }
        update(updateWrapper);
        
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		List<OrganizationCost> costLists = this.baseMapper.selectList(updateWrapper); 
        		
        		LambdaUpdateWrapper<OrganizationCost> queryWrapper = new LambdaUpdateWrapper<>();
        		queryWrapper.in(OrganizationCost::getOrganizationCode, organizationCode);
        		queryWrapper.eq(OrganizationCost::getCostName, costName);
        		queryWrapper.eq(OrganizationCost::getSmsType, smsType);
        		queryWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(OrganizationCost::getOperator)
    	    			.or().eq(OrganizationCost::getOperator, "")
    	    			);
        		List<OrganizationCost> upCostLists = this.baseMapper.selectList(queryWrapper); 
        		
            	List<OrganizationCost> newCosts = new ArrayList<OrganizationCost>();
            	for (int i = 0; i < organizationCode.length; i++) 
        		{
            		String thisOrgCode = organizationCode[i];
            		boolean needSaveFlag = true;
            		if(costLists!=null && costLists.size()>0)
                    {
            			for (int j = 0; j < costLists.size(); j++) 
                    	{
                    		OrganizationCost orgCost = costLists.get(j);
                    		String orgCode = orgCost.getOrganizationCode();
                    		if(thisOrgCode.equals(orgCode))
                    		{
                    			needSaveFlag = false;
                    			break;
                    		}
                		}
                    }
            		if(needSaveFlag)
            		{
            			if(upCostLists!=null && upCostLists.size()>0)
                        {
            				OrganizationCost orgCost = new OrganizationCost();
                    		boolean checkUpCostFlag = false;
                			for (int j = 0; j < upCostLists.size(); j++) 
                        	{
                        		OrganizationCost upOrgCost = upCostLists.get(j);
                        		String orgCode = upOrgCost.getOrganizationCode();
                        		if(thisOrgCode.equals(orgCode))
                        		{
                        			checkUpCostFlag = true;
                        			break;
                        		}
                    		}
                			if(checkUpCostFlag)
                			{
                				orgCost.setCostValue(costValue);
                				orgCost.setOrganizationCode(thisOrgCode);
                        		orgCost.setSmsType(Integer.valueOf(smsType).intValue());
                        		orgCost.setCostName(costName);
                        		orgCost.setState("1");
                        		orgCost.setChannelId("");
                        		orgCost.setDescription("");
                        		orgCost.setRemark(userName + "添加");
                        		orgCost.setCreateDate(new Date());
                        		orgCost.setOperator(operator);
                				newCosts.add(orgCost);
                			}
                        }
            		}
        		}
            	if(newCosts!=null && newCosts.size()>0)
            	{
            		addOrgCosts(newCosts);
            	}
        	}
        }
    }
    
    /**
     * 批量新增资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     */
    @Override
    public void addOrgCosts(String organizationCodes, String costName,String smsType, String costValue,String operator,String userName) 
    {
    	String[] organizationCode = organizationCodes.split(",");
    	List<OrganizationCost> costs = new ArrayList<OrganizationCost>();
    	for (int i = 0; i < organizationCode.length; i++) 
    	{
    		OrganizationCost orgCost = new OrganizationCost();
    		orgCost.setOrganizationCode(organizationCode[i]);
    		orgCost.setSmsType(Integer.valueOf(smsType).intValue());
    		orgCost.setCostName(costName);
    		orgCost.setCostValue(costValue);
    		orgCost.setState("1");
    		orgCost.setChannelId("");
    		orgCost.setDescription("");
    		orgCost.setRemark(userName + "添加");
    		orgCost.setCreateDate(new Date());
    		orgCost.setOperator(operator);
		}
    	addOrgCosts(costs);
    }
    
    /**
     * 批量新增资费
     * @param costs
     */
    @Override
    public void addOrgCosts(List<OrganizationCost> costs) 
    {
        this.saveBatch(costs);
    }
    
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
    @Override
    public void updateFloatCosts(String organizationCodes, String costName,String smsType, String costValue,String operator,String userName,String updateType) 
    {
    	String[] organizationCode = organizationCodes.split(",");
        LambdaUpdateWrapper<OrganizationCost> updateWrapper = new LambdaUpdateWrapper<>();
        if("0".equals(updateType))
        {
        	 updateWrapper.setSql("Cost_Value=Cost_Value-"+costValue);
        }
        else
        {
        	 updateWrapper.setSql("Cost_Value=Cost_Value+"+costValue);
        }
        updateWrapper.in(OrganizationCost::getOrganizationCode, organizationCode);
        updateWrapper.eq(OrganizationCost::getCostName, costName);
        updateWrapper.eq(OrganizationCost::getSmsType, smsType);
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		updateWrapper.eq(OrganizationCost::getOperator, operator);
        	}
        	else
        	{
        		updateWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(OrganizationCost::getOperator)
    	    			.or().eq(OrganizationCost::getOperator, "")
    	    			);
        	}
        }
        update(updateWrapper);
        
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		List<OrganizationCost> costLists = this.baseMapper.selectList(updateWrapper); 
        		
        		LambdaUpdateWrapper<OrganizationCost> queryWrapper = new LambdaUpdateWrapper<>();
        		queryWrapper.in(OrganizationCost::getOrganizationCode, organizationCode);
        		queryWrapper.eq(OrganizationCost::getCostName, costName);
        		queryWrapper.eq(OrganizationCost::getSmsType, smsType);
        		queryWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(OrganizationCost::getOperator)
    	    			.or().eq(OrganizationCost::getOperator, "")
    	    			);
        		List<OrganizationCost> upCostLists = this.baseMapper.selectList(queryWrapper); 
        		
            	List<OrganizationCost> newCosts = new ArrayList<OrganizationCost>();
            	for (int i = 0; i < organizationCode.length; i++) 
        		{
            		String thisOrgCode = organizationCode[i];
            		boolean needSaveFlag = true;
            		if(costLists!=null && costLists.size()>0)
                    {
            			for (int j = 0; j < costLists.size(); j++) 
                    	{
                    		OrganizationCost orgCost = costLists.get(j);
                    		String orgCode = orgCost.getOrganizationCode();
                    		if(thisOrgCode.equals(orgCode))
                    		{
                    			needSaveFlag = false;
                    			break;
                    		}
                		}
                    }
            		if(needSaveFlag)
            		{
            			if(upCostLists!=null && upCostLists.size()>0)
                        {
            				OrganizationCost orgCost = new OrganizationCost();
                    		int costValueInt = Integer.valueOf(costValue).intValue();
                    		boolean checkUpCostFlag = false;
                			for (int j = 0; j < upCostLists.size(); j++) 
                        	{
                        		OrganizationCost upOrgCost = upCostLists.get(j);
                        		String orgCode = upOrgCost.getOrganizationCode();
                        		String orgCostValue = upOrgCost.getCostValue();
                        		if(thisOrgCode.equals(orgCode))
                        		{
                        			int newCostInt = Integer.valueOf(orgCostValue).intValue();
                        			int costInt = newCostInt;
                        			if("0".equals(updateType))
                        		    {
                        				costInt = newCostInt - costValueInt ;
                        		    }
                        		    else
                        		    {
                        		    	costInt = newCostInt + costValueInt;
                        		    }
                        			orgCost.setCostValue(String.valueOf(costInt));
                        			checkUpCostFlag = true;
                        			break;
                        		}
                    		}
                			if(checkUpCostFlag)
                			{
                				orgCost.setOrganizationCode(thisOrgCode);
                        		orgCost.setSmsType(Integer.valueOf(smsType).intValue());
                        		orgCost.setCostName(costName);
                        		orgCost.setState("1");
                        		orgCost.setChannelId("");
                        		orgCost.setDescription("");
                        		orgCost.setRemark(userName + "添加");
                        		orgCost.setCreateDate(new Date());
                        		orgCost.setOperator(operator);
                				newCosts.add(orgCost);
                			}
                        }
            		}
        		}
            	if(newCosts!=null && newCosts.size()>0)
            	{
            		addOrgCosts(newCosts);
            	}
        	}
        }
    }
    
    @Override
    public List<AreaCode> getOrgAreaCodeList(String orgCode) 
    {
		// TODO 设置查询条件
    	LambdaQueryWrapper<OrganizationCost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrganizationCost::getOrganizationCode,orgCode);
        List<OrganizationCost> orgCostLists = this.baseMapper.selectList(queryWrapper);
        
        List<AreaCode> smsNumberAreaCodeList = new ArrayList<AreaCode>();
        if(orgCostLists!=null&&orgCostLists.size()>0)
        {
        	List<String>  areaCodeList= new ArrayList<String>();
        	Map<String,String> areaCodeMap = new HashMap<String, String>();
        	for (int i = 0; i < orgCostLists.size(); i++) 
        	{
        		String costName = orgCostLists.get(i).getCostName();
        		if(areaCodeMap.get(costName)!=null&&!"".equals(areaCodeMap.get(costName)))
        		{
        			continue;
        		}
        		else
        		{
        			areaCodeMap.put(costName, costName);
        			areaCodeList.add(costName);
        		}
			}
        	smsNumberAreaCodeList = DatabaseCache.getAreaCodeListScreen(areaCodeList);
        }
        return smsNumberAreaCodeList;
    }
}
