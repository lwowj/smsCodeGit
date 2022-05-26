package com.hero.sms.entity.organization.ext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.utils.StringUtil;

import lombok.Data;

@Data
public class OrganizationExt extends Organization {

    private List<OrganizationProperty> propertys;
    private List<OrganizationCost> costs;

    public OrganizationCost getOrganizationCost(Integer smsType,String costName){
        if (costs == null || costs.size() == 0){
            return null;
        }
        List<OrganizationCost> tmp = costs.stream()
                .filter(organizationCost -> organizationCost.getSmsType().intValue() == smsType.intValue() && organizationCost.getCostName().equals(costName) && StringUtil.isBlank(organizationCost.getOperator()))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(tmp)) return tmp.get(0);
        return null;
    }
    
    /**
     * @begin 2021-11-09
     * @param smsType
     * @param costName
     * @param operator 电信 CTCC、联通 CUCC、移动 CMCC
     * @return
     */
    public OrganizationCost getOrganizationCost(Integer smsType,String costName,String operator){
        if (costs == null || costs.size() == 0){
            return null;
        }
        List<OrganizationCost> tmp = costs.stream()
                .filter(organizationCost -> organizationCost.getSmsType().intValue() == smsType.intValue() && organizationCost.getCostName().equals(costName) && operator.equals(organizationCost.getOperator()))
                .collect(Collectors.toList());
        //若针对运营商的配置未获得，则取通用配置
        if(CollectionUtils.isEmpty(tmp)) 
        {
        	return getOrganizationCost(smsType, costName);
        }
        if(CollectionUtils.isNotEmpty(tmp)) return tmp.get(0);
        return null;
    }
    
    /**
     * @begin 2021-11-13
     * @param smsType
     * @param costName
     * @return
     */
    public List<OrganizationCost> getOrganizationCostList(Integer smsType,String costName){
        if (costs == null || costs.size() == 0){
            return null;
        }
        List<OrganizationCost> tmp = costs.stream()
                .filter(organizationCost -> organizationCost.getSmsType().intValue() == smsType.intValue() && organizationCost.getCostName().equals(costName) && StringUtil.isNotBlank(organizationCost.getOperator()))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(tmp)) return tmp;
        return null;
    }
    
    public List<OrganizationProperty> getPropertyByPropertyType(Integer propertyType){
    	if (propertys == null || propertys.size() == 0){
            return null;
        }
        List<OrganizationProperty> list = propertys.stream()
                .filter(property -> property.getPropertyType().intValue() ==  propertyType)
                .collect(Collectors.toList());
        return list;
    }
    
    public OrganizationProperty getProperty(Integer propertyType,String name) {
    	if (propertys == null || propertys.size() == 0){
            return null;
        }
    	
    	Optional<OrganizationProperty> orgProperty = propertys.stream().filter(property -> property.getPropertyType().intValue() ==  propertyType && property.getName().equals(name)).findFirst();
    	if(orgProperty.isPresent()) {
    		return orgProperty.get();
    	}
        return null;
    }
}
