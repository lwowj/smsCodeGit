package com.hero.sms.service.impl.agent;

import java.util.ArrayList;
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
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.mapper.agent.AgentCostMapper;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.utils.StringUtil;

/**
 * 商户代理资费 Service实现
 *
 * @author Administrator
 * @date 2020-03-06 10:05:33
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentCostServiceImpl extends ServiceImpl<AgentCostMapper, AgentCost> implements IAgentCostService {

    @Autowired
    private AgentCostMapper agentCostMapper;
    @Autowired
    private IAgentService agentService;

    @Override
    public IPage<AgentCost> findAgentCosts(QueryRequest request, AgentCost agentCost) {
        LambdaQueryWrapper<AgentCost> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<AgentCost> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentCost> findAgentCosts(AgentCost agentCost) {
	    LambdaQueryWrapper<AgentCost> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        queryWrapper.eq(AgentCost::getAgentId,agentCost.getAgentId());
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createAgentCost(AgentCost agentCost) {
        this.save(agentCost);
    }

    @Override
    @Transactional
    public void updateAgentCost(AgentCost agentCost) {
        this.saveOrUpdate(agentCost);
    }

    /**
     * 代理端添加资费
     * @param agentCosts
     * @param upAgent
     */
    @Override
    @Transactional
    public void updateAgentCosts(List<AgentCost> agentCosts, Agent upAgent) {
        if(agentCosts.size() > 0){
            Integer oldAgentId = agentCosts.get(0).getAgentId();
            Agent oldAgent = this.agentService.getById(oldAgentId);
            if(!upAgent.getId().equals(oldAgent.getUpAgentId())){
                throw new FebsException("只能修改本代理的下级代理");
            }
            LambdaQueryWrapper<AgentCost> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(AgentCost::getAgentId,oldAgentId);
            this.remove(deleteWrapper);
            LambdaQueryWrapper<AgentCost> agentWrapper = new LambdaQueryWrapper<>();
            agentWrapper.eq(AgentCost::getAgentId,upAgent.getId());
            List<AgentCost> upAgentCosts = this.list(agentWrapper);
            //key 新增运营商标识 StringUtil.isBlank(a.getOperator())?"":a.getOperator()
            Map<String,String> agentMap = upAgentCosts.stream().collect(Collectors.toMap(a -> a.getName() + a.getSmsType() + (StringUtil.isBlank(a.getOperator())?"":a.getOperator()), a -> a.getValue()));
            Map costMap = new HashMap();
            Map<String,String> chinaMap = new HashMap<String,String>();
            Iterator<AgentCost> it = agentCosts.iterator();
            while (it.hasNext()) {
                AgentCost agent = it.next();
                if (StringUtils.isEmpty(agent.getName()) || StringUtils.isEmpty(agent.getValue()) || agent.getSmsType() == null) {
                    it.remove();
                }else {
                	//新增区域判断。非国内不设置运营商
                	if(!agent.getName().equals(SmsNumberAreaCodeEnums.China.getInArea()))
                	{
                		agent.setOperator(null);
                	}
                	else 
                	{
                		if(StringUtil.isBlank(agent.getOperator()))
                		{
                			chinaMap.put(SmsNumberAreaCodeEnums.China.getInArea(), SmsNumberAreaCodeEnums.China.getInArea());
                		}
                		else
                		{
                			chinaMap.put(agent.getOperator(), agent.getOperator());
                		}
                	}
                    String agentCost = agentMap.get(agent.getName()+agent.getSmsType()+ (StringUtil.isBlank(agent.getOperator())?"":agent.getOperator()));
                    if(StringUtils.isEmpty(agentCost)){
                        throw new FebsException("该资费类型上级代理未配置！");
                    }
                    if(Integer.parseInt(agentCost) > Integer.parseInt(agent.getValue())){
                        throw new FebsException("该资费类型配置不合理,请检查上级代理同类型配置！");
                    }
                    //新增资费不为负数判断
                    if (Integer.parseInt(agent.getValue()) < 0) {
                    	throw new FebsException("该资费类型配置不合理,资费不能为负！");
                    }
                    costMap.put(agent.getName() + agent.getSmsType() + (StringUtil.isBlank(agent.getOperator())?"":agent.getOperator()),agent.getValue());
                    agent.setDescription("");
                    agent.setRemark(upAgent.getAgentAccount() + "添加");
                    agent.setCreateDate(new Date());
                }
            }
            if(agentCosts.size()!=costMap.size()) {
                throw new FebsException("相同的资费类型只能添加一条！");
            }
            if(chinaMap != null && !chinaMap.isEmpty()) 
            {
            	if(chinaMap.get(SmsNumberAreaCodeEnums.China.getInArea()) == null)
            	{
            		 throw new FebsException("国内资费必须添加一条运营商开放的配置！");
            	}
            }
            this.saveBatch(agentCosts);
        }
    }

    @Override
    @Transactional
    public void updateAgentCosts(List<AgentCost> agentCosts,String userName) {
        if(agentCosts.size() > 0){
            Integer oldAgentId = agentCosts.get(0).getAgentId();
            Agent oldAgent = this.agentService.getById(oldAgentId);
            Map<String,String> agentMap = null;
            if(oldAgent.getUpAgentId() != null){
                LambdaQueryWrapper<AgentCost> agentWrapper = new LambdaQueryWrapper<>();
                agentWrapper.eq(AgentCost::getAgentId,oldAgent.getUpAgentId());
                List<AgentCost> upAgentCosts = this.list(agentWrapper);
                //key 新增运营商标识 StringUtil.isBlank(a.getOperator())?"":a.getOperator()
                agentMap = upAgentCosts.stream().collect(Collectors.toMap(a -> a.getName() + a.getSmsType() + (StringUtil.isBlank(a.getOperator())?"":a.getOperator()), a -> a.getValue()));
            }

            LambdaQueryWrapper<AgentCost> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(AgentCost::getAgentId,agentCosts.get(0).getAgentId());
            this.remove(deleteWrapper);
            Map costMap = new HashMap();
            Map<String,String> chinaMap = new HashMap<String,String>();
            Iterator<AgentCost> it = agentCosts.iterator();
            while (it.hasNext()) {
                AgentCost agent = it.next();
                if (StringUtils.isEmpty(agent.getName()) || StringUtils.isEmpty(agent.getValue()) || agent.getSmsType() == null) {
                    it.remove();
                }else {
                	//新增区域判断。非国内不设置运营商
                	if(!agent.getName().equals(SmsNumberAreaCodeEnums.China.getInArea()))
                	{
                		agent.setOperator(null);
                	}
                	else 
                	{
                		if(StringUtil.isBlank(agent.getOperator()))
                		{
                			chinaMap.put(SmsNumberAreaCodeEnums.China.getInArea(), SmsNumberAreaCodeEnums.China.getInArea());
                		}
                		else
                		{
                			chinaMap.put(agent.getOperator(), agent.getOperator());
                		}
                	}
                    if(oldAgent.getUpAgentId() != null){
                        String agentCost = agentMap.get(agent.getName()+agent.getSmsType() + (StringUtil.isBlank(agent.getOperator())?"":agent.getOperator()));
                        if(StringUtils.isEmpty(agentCost)){
                            throw new FebsException("该资费类型上级代理未配置！");
                        }
                        if(Integer.parseInt(agentCost) > Integer.parseInt(agent.getValue())){
                            throw new FebsException("该资费类型配置不合理,请检查上级代理同类型配置！");
                        }
                    }
                    //新增资费不为负数判断
                    if (Integer.parseInt(agent.getValue()) < 0) {
                    	throw new FebsException("该资费类型配置不合理,资费不能为负！");
                    }
                    costMap.put(agent.getName()+agent.getSmsType()+ (StringUtil.isBlank(agent.getOperator())?"":agent.getOperator()),agent.getValue());
                    agent.setDescription("");
                    agent.setRemark(userName + "添加");
                    agent.setCreateDate(new Date());
                }
            }
            if(agentCosts.size()!=costMap.size()) {
                throw new FebsException("相同的资费类型只能添加一条！");
            }
            if(chinaMap != null && !chinaMap.isEmpty()) 
            {
            	if(chinaMap.get(SmsNumberAreaCodeEnums.China.getInArea()) == null)
            	{
            		 throw new FebsException("国内资费必须添加一条运营商开放的配置！");
            	}
            }
            this.saveBatch(agentCosts);
        }
    }

    @Override
    @Transactional
    public void deleteAgentCost(AgentCost agentCost) {
        LambdaQueryWrapper<AgentCost> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}
    
    /**
     * 批量更新资费
     * @param organizationCodes
     * @param costName
     * @param smsType
     * @param costValue
     */
    @Override
    public void updateCosts(String agentIds, String costName,String smsType, String costValue) {
        updateCosts(agentIds, costName, smsType, costValue, null,null);
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
    public void updateCosts(String agentIds, String costName,String smsType, String costValue,String operator,String userName) 
    {
    	String[] agentId = agentIds.split(",");
        LambdaUpdateWrapper<AgentCost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AgentCost::getValue, costValue);
        updateWrapper.in(AgentCost:: getAgentId, agentId);
        updateWrapper.eq(AgentCost::getName, costName);
        updateWrapper.eq(AgentCost::getSmsType, smsType);
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		updateWrapper.eq(AgentCost::getOperator, operator);
        	}
        	else
        	{
        		updateWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(AgentCost::getOperator)
    	    			.or().eq(AgentCost::getOperator, "")
    	    			);
        	}
        }
        update(updateWrapper);
        
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		List<AgentCost> costLists = this.baseMapper.selectList(updateWrapper);
        		
        		LambdaUpdateWrapper<AgentCost> queryWrapper = new LambdaUpdateWrapper<>();
        		queryWrapper.in(AgentCost:: getAgentId, agentId);
        		queryWrapper.eq(AgentCost::getName, costName);
        		queryWrapper.eq(AgentCost::getSmsType, smsType);
        		queryWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(AgentCost::getOperator)
    	    			.or().eq(AgentCost::getOperator, "")
    	    			);
        		
        		List<AgentCost> upCostLists = this.baseMapper.selectList(queryWrapper); 
        		
            	List<AgentCost> newCosts = new ArrayList<AgentCost>();
            	for (int i = 0; i < agentId.length; i++) 
        		{
            		int thisAgentId = Integer.valueOf(agentId[i]).intValue();
            		boolean needSaveFlag = true;
            		if(costLists!=null && costLists.size()>0)
                    {
            			for (int j = 0; j < costLists.size(); j++) 
                    	{
            				AgentCost agentCost = costLists.get(j);
                    		int agentIdInt = agentCost.getAgentId();
                    		if(thisAgentId == agentIdInt)
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
            				AgentCost saveAgentCost = new AgentCost();
                    		boolean checkUpCostFlag = false;
                			for (int j = 0; j < upCostLists.size(); j++) 
                        	{
                				AgentCost upAgentCost = upCostLists.get(j);
                        		int upAgentId = upAgentCost.getAgentId();
                        		if(thisAgentId == upAgentId)
                        		{
                        			checkUpCostFlag = true;
                        			break;
                        		}
                    		}
                			if(checkUpCostFlag)
                			{
                				saveAgentCost.setValue(costValue);
                				saveAgentCost.setAgentId(thisAgentId);
                				saveAgentCost.setSmsType(Integer.valueOf(smsType).intValue());
                    			saveAgentCost.setName(costName);
                    			saveAgentCost.setChannelId("");
                    			saveAgentCost.setDescription("");
                    			saveAgentCost.setRemark(userName + "添加");
                        		saveAgentCost.setCreateDate(new Date());
                        		saveAgentCost.setOperator(operator);
                        		newCosts.add(saveAgentCost);
                			}
                        }
            		}
        		}
            	if(newCosts!=null && newCosts.size()>0)
            	{
            		addAgentCosts(newCosts);
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
    public void addAgentCosts(String agentIds, String costName,String smsType, String costValue,String operator,String userName) 
    {
    	String[] agentId = agentIds.split(",");
    	List<AgentCost> costs = new ArrayList<AgentCost>();
    	for (int i = 0; i < agentId.length; i++) 
    	{
    		AgentCost saveAgentCost = new AgentCost();
			saveAgentCost.setAgentId(Integer.valueOf(agentId[i]).intValue());
			saveAgentCost.setSmsType(Integer.valueOf(smsType).intValue());
			saveAgentCost.setName(costName);
			saveAgentCost.setValue(costValue);
			saveAgentCost.setChannelId("");
			saveAgentCost.setDescription("");
			saveAgentCost.setRemark(userName + "添加");
    		saveAgentCost.setCreateDate(new Date());
    		if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
            {
            	if(StringUtil.isNotBlank(operator))
            	{
            		saveAgentCost.setOperator(operator);
            	}
            }
		}
    	addAgentCosts(costs);
    }
    
    /**
     * 批量新增资费
     * @param costs
     */
    @Override
    public void addAgentCosts(List<AgentCost> agentCosts) 
    {
        this.saveBatch(agentCosts);
    }
    
    /**
     * 批量浮动更新资费
     * @param agentIds
     * @param costName
     * @param smsType
     * @param costValue
     * @param operator
     * @param userName
     * @param updateType
     */
    @Override
    public void updateFloatCosts(String agentIds, String costName,String smsType, String costValue,String operator,String userName,String updateType) 
    {
    	String[] agentId = agentIds.split(",");
        LambdaUpdateWrapper<AgentCost> updateWrapper = new LambdaUpdateWrapper<>();
        if("0".equals(updateType))
        {
        	 updateWrapper.setSql("Value=Value-"+costValue);
        }
        else
        {
        	 updateWrapper.setSql("Value=Value+"+costValue);
        }
        updateWrapper.in(AgentCost:: getAgentId, agentId);
        updateWrapper.eq(AgentCost::getName, costName);
        updateWrapper.eq(AgentCost::getSmsType, smsType);
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		updateWrapper.eq(AgentCost::getOperator, operator);
        	}
        	else
        	{
        		updateWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(AgentCost::getOperator)
    	    			.or().eq(AgentCost::getOperator, "")
    	    			);
        	}
        }
        update(updateWrapper);
        
        if(costName.equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	if(StringUtil.isNotBlank(operator))
        	{
        		List<AgentCost> costLists = this.baseMapper.selectList(updateWrapper);
        		
        		LambdaUpdateWrapper<AgentCost> queryWrapper = new LambdaUpdateWrapper<>();
        		queryWrapper.in(AgentCost:: getAgentId, agentId);
        		queryWrapper.eq(AgentCost::getName, costName);
        		queryWrapper.eq(AgentCost::getSmsType, smsType);
        		queryWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(AgentCost::getOperator)
    	    			.or().eq(AgentCost::getOperator, "")
    	    			);
        		
        		List<AgentCost> upCostLists = this.baseMapper.selectList(queryWrapper); 
        		
            	List<AgentCost> newCosts = new ArrayList<AgentCost>();
            	for (int i = 0; i < agentId.length; i++) 
        		{
            		int thisAgentId = Integer.valueOf(agentId[i]).intValue();
            		boolean needSaveFlag = true;
            		if(costLists!=null && costLists.size()>0)
                    {
            			for (int j = 0; j < costLists.size(); j++) 
                    	{
            				AgentCost agentCost = costLists.get(j);
                    		int agentIdInt = agentCost.getAgentId();
                    		if(thisAgentId == agentIdInt)
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
            				AgentCost saveAgentCost = new AgentCost();
                    		int costValueInt = Integer.valueOf(costValue).intValue();
                    		boolean checkUpCostFlag = false;
                			for (int j = 0; j < upCostLists.size(); j++) 
                        	{
                				AgentCost upAgentCost = upCostLists.get(j);
                        		int upAgentId = upAgentCost.getAgentId();
                        		String upAgentCostValue = upAgentCost.getValue();
                        		if(thisAgentId == upAgentId)
                        		{
                        			int newCostInt = Integer.valueOf(upAgentCostValue).intValue();
                        			int costInt = newCostInt;
                        			if("0".equals(updateType))
                        		    {
                        				costInt = newCostInt - costValueInt ;
                        		    }
                        		    else
                        		    {
                        		    	costInt = newCostInt + costValueInt;
                        		    }
                        			saveAgentCost.setValue(String.valueOf(costInt));
                        			checkUpCostFlag = true;
                        			break;
                        		}
                    		}
                			if(checkUpCostFlag)
                			{
                				saveAgentCost.setAgentId(thisAgentId);
                				saveAgentCost.setSmsType(Integer.valueOf(smsType).intValue());
                    			saveAgentCost.setName(costName);
                    			saveAgentCost.setChannelId("");
                    			saveAgentCost.setDescription("");
                    			saveAgentCost.setRemark(userName + "添加");
                        		saveAgentCost.setCreateDate(new Date());
                        		saveAgentCost.setOperator(operator);
                        		newCosts.add(saveAgentCost);
                			}
                        }
            		}
        		}
            	if(newCosts!=null && newCosts.size()>0)
            	{
            		addAgentCosts(newCosts);
            	}
        	}
        }
    }
    
    @Override
    public List<AreaCode> getAgentAreaCodeList(int agentId) {
	    LambdaQueryWrapper<AgentCost> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        queryWrapper.eq(AgentCost::getAgentId,agentId);
        List<AgentCost> agCostLists = this.baseMapper.selectList(queryWrapper);
        
        List<AreaCode> smsNumberAreaCodeList = new ArrayList<AreaCode>();
        if(agCostLists!=null&&agCostLists.size()>0)
        {
        	List<String>  areaCodeList= new ArrayList<String>();
        	Map<String,String> areaCodeMap = new HashMap<String, String>();
        	for (int i = 0; i < agCostLists.size(); i++) 
        	{
        		String name = agCostLists.get(i).getName();
        		if(areaCodeMap.get(name)!=null&&!"".equals(areaCodeMap.get(name)))
        		{
        			continue;
        		}
        		else
        		{
        			areaCodeMap.put(name, name);
        			areaCodeList.add(name);
        		}
			}
        	smsNumberAreaCodeList = DatabaseCache.getAreaCodeListScreen(areaCodeList);
        }
        return smsNumberAreaCodeList;
    }
}
