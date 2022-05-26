package com.hero.sms.service.impl.organization;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.enums.organization.OrgInterfaceTypeEnums;
import com.hero.sms.enums.organization.OrganizationPropertyNameEnums;
import com.hero.sms.mapper.organization.OrganizationPropertyMapper;
import com.hero.sms.service.organization.IOrganizationPropertyService;
import com.hero.sms.utils.RandomUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户属性 Service实现
 *
 * @author Administrator
 * @date 2020-05-01 19:49:03
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationPropertyServiceImpl extends ServiceImpl<OrganizationPropertyMapper, OrganizationProperty> implements IOrganizationPropertyService {

    @Autowired
    private OrganizationPropertyMapper organizationPropertyMapper;

    @Override
    public IPage<OrganizationProperty> findOrganizationPropertys(QueryRequest request, OrganizationProperty organizationProperty) {
        LambdaQueryWrapper<OrganizationProperty> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<OrganizationProperty> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationProperty> findOrganizationPropertys(OrganizationProperty organizationProperty) {
	    LambdaQueryWrapper<OrganizationProperty> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationProperty(OrganizationProperty organizationProperty) {
        organizationProperty.setCreateTime(new Date());
        this.save(organizationProperty);
    }
    @Override
    @Transactional
    public void createDefaultSmppProperty(String orgCode, String remark){

        if (StringUtils.isBlank(orgCode)){
            throw new FebsException("（创建默认属性）商户编号不能为空！");
        }
        Integer smppTypeValue = OrgInterfaceTypeEnums.Smpp.getCode();
        LambdaQueryWrapper<OrganizationProperty> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrganizationProperty::getOrganizationCode,orgCode);
        queryWrapper.eq(OrganizationProperty::getPropertyType,smppTypeValue);

        int count = this.count(queryWrapper);
        if (count > 1){
            log.info(String.format("商户【%s】已存在smpp相关属性，创建默认SMPP属性中止",orgCode));
            return;
        }

        List<OrganizationProperty> list = Lists.newArrayList();

        //systemId
        String systemIdName = OrganizationPropertyNameEnums.SystemId.getCode();
        String systemIdValue = orgCode;
        OrganizationProperty systemId = new OrganizationProperty(orgCode,smppTypeValue,systemIdName,systemIdValue,remark);
        list.add(systemId);
        //密码
        String passwordName = OrganizationPropertyNameEnums.Password.getCode();
        String passwordValue = RandomUtil.randomStr(8);
        OrganizationProperty password = new OrganizationProperty(orgCode,smppTypeValue,passwordName,passwordValue,remark);
        list.add(password);
        //最大连接数
        String maxChannelsName = OrganizationPropertyNameEnums.MaxChannels.getCode();
        Code maxChannelCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","smppMaxChannels");
        String maxChannelsValue = maxChannelCode.getName();
        OrganizationProperty maxChannels = new OrganizationProperty(orgCode,smppTypeValue,maxChannelsName,maxChannelsValue,remark);
        list.add(maxChannels);
        //流速
        String readLimitName = OrganizationPropertyNameEnums.ReadLimit.getCode();
        Code readLimitCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","smppReadLimit");
        String readLimitValue = readLimitCode.getName();
        OrganizationProperty readLimit = new OrganizationProperty(orgCode,smppTypeValue,readLimitName,readLimitValue,remark);
        list.add(readLimit);

        this.saveBatch(list);
    }

    @Override
    @Transactional
    public void updateOrganizationProperty(OrganizationProperty organizationProperty) {
        this.saveOrUpdate(organizationProperty);
    }

    @Override
    @Transactional
    public void deleteOrganizationProperty(OrganizationProperty organizationProperty) {
        LambdaQueryWrapper<OrganizationProperty> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationPropertys(String[] organizationPropertyIds) {
        List<String> list = Arrays.asList(organizationPropertyIds);
        this.removeByIds(list);
    }

    @Override
    public void updateSmsChannelProperties(List<OrganizationProperty> organizationProperties, String username) {
        if (organizationProperties.size() > 0){
            LambdaQueryWrapper<OrganizationProperty> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(OrganizationProperty::getOrganizationCode,organizationProperties.get(0).getOrganizationCode());
            deleteWrapper.eq(OrganizationProperty::getPropertyType,organizationProperties.get(0).getPropertyType());
            this.remove(deleteWrapper);
            List<OrganizationProperty> saveList = organizationProperties.stream()
                    .filter(item -> {
                        boolean result = StringUtils.isNotBlank(item.getName());
                        if (result){
                            item.setDescription("");
                            item.setRemark(username + "添加");
                            item.setCreateTime(new Date());
                        }
                        return result;
                    }).collect(Collectors.toList());

            this.saveBatch(saveList);
        }
    }
}
