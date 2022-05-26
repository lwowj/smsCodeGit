package com.hero.sms.service.impl.rechargeOrder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.common.utils.RandomStringUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderExt;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderQuery;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import com.hero.sms.enums.common.RecordedTypeEnums;
import com.hero.sms.mapper.rechargeOrder.OrganizationRechargeOrderMapper;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商户充值订单 Service实现
 *
 * @author Administrator
 * @date 2020-03-12 17:57:48
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationRechargeOrderServiceImpl extends ServiceImpl<OrganizationRechargeOrderMapper, OrganizationRechargeOrder> implements IOrganizationRechargeOrderService {

    @Autowired
    private OrganizationRechargeOrderMapper organizationRechargeOrderMapper;
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IAgentService agentService;

    @Override
    public IPage<OrganizationRechargeOrder> findOrganizationRechargeOrders(QueryRequest request, OrganizationRechargeOrder organizationRechargeOrder, Date orgReqStartTime, Date orgReqEndTime) {
        LambdaQueryWrapper<OrganizationRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getOrganizationCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getOrganizationCode,organizationRechargeOrder.getOrganizationCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getOrderNo())){
            queryWrapper.eq(OrganizationRechargeOrder::getOrderNo,organizationRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getNetwayCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getNetwayCode,organizationRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getReqStateCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getReqStateCode,organizationRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRechargeType())){
            queryWrapper.eq(OrganizationRechargeOrder::getRechargeType,organizationRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getRechargeStateCode,organizationRechargeOrder.getRechargeStateCode());
        }
        if(orgReqStartTime != null){
            queryWrapper.ge(OrganizationRechargeOrder::getReqTime,orgReqStartTime);
        }else{
            queryWrapper.ge(OrganizationRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(orgReqEndTime != null){
            queryWrapper.le(OrganizationRechargeOrder::getReqTime,orgReqEndTime);
        }else {
            queryWrapper.le(OrganizationRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        /**
         * 2021-04-07
         * 新增入账方式条件
         */
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRecordedType())){
            queryWrapper.eq(OrganizationRechargeOrder::getRecordedType,organizationRechargeOrder.getRecordedType());
        }
        queryWrapper.orderByDesc(OrganizationRechargeOrder::getId);
        Page<OrganizationRechargeOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<OrganizationRechargeOrderExt> extPage(QueryRequest request, OrganizationRechargeOrderQuery organizationRechargeOrderQuery){
        Page<OrganizationRechargeOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<OrganizationRechargeOrderExt> datas = this.baseMapper.extPage(page, organizationRechargeOrderQuery);
        datas.getRecords().stream().forEach(item ->{
            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrganizationCode());
            item.setOrgName(orgName);
            if (item.getAgentId() != null){
            	item.setAgentName(DatabaseCache.getAgentNameByAgentId(item.getAgentId()));
            }
        });
        return datas;
    }

    /**
     * 左联商户表查询
     * @param request
     * @param organizationRechargeOrder
     * @param agent
     * @return
     */
    @Override
    public IPage<OrganizationRechargeOrder> findOrganizationRechargeOrders(QueryRequest request, OrganizationRechargeOrder organizationRechargeOrder,Agent agent, Date orgReqStartTime, Date orgReqEndTime) {
        QueryWrapper<OrganizationRechargeOrder> queryWrapper = new QueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq("g.`Agent_Id`",agent.getId());
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getOrganizationCode())){
            queryWrapper.eq("o.organization_code",organizationRechargeOrder.getOrganizationCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getOrderNo())){
            queryWrapper.eq("o.`order_no`",organizationRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getNetwayCode())){
            queryWrapper.eq("o.`netway_code`",organizationRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getReqStateCode())){
            queryWrapper.eq("o.`req_state_code`",organizationRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRechargeType())){
            queryWrapper.eq("o.`recharge_type`",organizationRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq("o.`recharge_state_code`",organizationRechargeOrder.getRechargeStateCode());
        }
        if(orgReqStartTime != null){
            queryWrapper.ge("o.`req_time`",orgReqStartTime);
        }else{
            queryWrapper.ge("o.`req_time`",DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(orgReqEndTime != null){
            queryWrapper.le("o.`req_time`",orgReqEndTime);
        }else {
            queryWrapper.le("o.`req_time`",DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        /**
         * 2021-04-07
         * 新增入账方式条件
         */
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRecordedType())){
            queryWrapper.eq("o.`recorded_type`",organizationRechargeOrder.getRecordedType());
        }
        queryWrapper.orderByDesc("o.id");
        Page<OrganizationRechargeOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.organizationRechargeOrderMapper.selectByOrganizationPage(page, queryWrapper);
    }

    @Override
    public List<StatisticBean> statisticBeanList(OrganizationRechargeOrderQuery organizationRechargeOrder) {
        LambdaQueryWrapper<OrganizationRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getOrganizationCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getOrganizationCode,organizationRechargeOrder.getOrganizationCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getOrderNo())){
            queryWrapper.eq(OrganizationRechargeOrder::getOrderNo,organizationRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getNetwayCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getNetwayCode,organizationRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getReqStateCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getReqStateCode,organizationRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRechargeType())){
            queryWrapper.eq(OrganizationRechargeOrder::getRechargeType,organizationRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq(OrganizationRechargeOrder::getRechargeStateCode,organizationRechargeOrder.getRechargeStateCode());
        }
        if(organizationRechargeOrder.getOrgReqStartTime() != null){
            queryWrapper.ge(OrganizationRechargeOrder::getReqTime,organizationRechargeOrder.getOrgReqStartTime());
        }else{
            queryWrapper.ge(OrganizationRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(organizationRechargeOrder.getOrgReqEndTime() != null){
            queryWrapper.le(OrganizationRechargeOrder::getReqTime,organizationRechargeOrder.getOrgReqEndTime());
        }else {
            queryWrapper.le(OrganizationRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        /**
         * 2021-04-07
         * 新增入账方式条件
         */
        if(StringUtils.isNotEmpty(organizationRechargeOrder.getRecordedType())){
            queryWrapper.eq(OrganizationRechargeOrder::getRecordedType,organizationRechargeOrder.getRecordedType());
        }
        
        /**
         * 2021-09-25
         * 新增代理商查询条件
         */
        if (StringUtils.isNotEmpty(organizationRechargeOrder.getAgentId())){
            queryWrapper.inSql(OrganizationRechargeOrder::getOrganizationCode
                    ,String.format("SELECT Organization_Code from t_organization where Agent_Id = %s",organizationRechargeOrder.getAgentId()));
        }
        
        queryWrapper.groupBy(OrganizationRechargeOrder::getNetwayCode);
        return this.baseMapper.statisticRechargeAmount(queryWrapper);
    }
    @Override
    public List<OrganizationRechargeOrder> findOrganizationRechargeOrders(OrganizationRechargeOrder organizationRechargeOrder) {
	    LambdaQueryWrapper<OrganizationRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder) {
        if(organizationRechargeOrder == null || StringUtils.isEmpty(organizationRechargeOrder.getOrganizationCode())  || organizationRechargeOrder.getRechargeAmount() < 0){
            throw new FebsException("账号信息不完整");
        }
        Organization Organization = this.organizationService.queryOrgByCodeForUpdate(organizationRechargeOrder.getOrganizationCode());
        if(Organization == null || !Organization.getDataMd5().equals(organizationService.getDataMd5(Organization))){
            throw new FebsException("商户信息错误");
        }
        organizationRechargeOrder.setCreateTime(new Date());
        organizationRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        organizationRechargeOrder.setPayChannelId("");
        organizationRechargeOrder.setPayMerchNo("");
        organizationRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","平台充值").getCode());
        organizationRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setAvailableAmount(new Long(Organization.getAvailableAmount()).intValue() + organizationRechargeOrder.getRechargeAmount());
        organizationRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        organizationRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        organizationRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setRecordedType(RecordedTypeEnums.Booked.getCode());
        this.save(organizationRechargeOrder);
        Organization.setAmount(Organization.getAmount() + organizationRechargeOrder.getRechargeAmount());
        Organization.setAvailableAmount(Organization.getAvailableAmount() + organizationRechargeOrder.getRechargeAmount());
        Organization.setDataMd5(organizationService.getDataMd5(Organization));
        organizationService.updateById(Organization);
    }

    /**
     * 商户扣减额度
     * @param organizationRechargeOrder
     */
    @Override
    @Transactional
    public void deducting(OrganizationRechargeOrder organizationRechargeOrder) {
        if(organizationRechargeOrder == null || StringUtils.isEmpty(organizationRechargeOrder.getOrganizationCode())  || organizationRechargeOrder.getRechargeAmount() < 0){
            throw new FebsException("账号信息不完整");
        }
        Organization Organization = this.organizationService.queryOrgByCodeForUpdate(organizationRechargeOrder.getOrganizationCode());
        if(Organization == null || !Organization.getDataMd5().equals(organizationService.getDataMd5(Organization))){
            throw new FebsException("商户信息错误");
        }
        organizationRechargeOrder.setCreateTime(new Date());
        organizationRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        organizationRechargeOrder.setPayChannelId("");
        organizationRechargeOrder.setPayMerchNo("");
        organizationRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","平台充值").getCode());
        organizationRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setAvailableAmount(new Long(Organization.getAvailableAmount()).intValue() - organizationRechargeOrder.getRechargeAmount());
        organizationRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        organizationRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        organizationRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setRecordedType(RecordedTypeEnums.Booked.getCode());
        this.save(organizationRechargeOrder);
        Organization.setAmount(Organization.getAmount() - organizationRechargeOrder.getRechargeAmount());
        Organization.setAvailableAmount(Organization.getAvailableAmount() - organizationRechargeOrder.getRechargeAmount());
        Organization.setDataMd5(organizationService.getDataMd5(Organization));
        organizationService.updateById(Organization);
    }

    /**
     * 代理端充值
     * @param organizationRechargeOrder
     * @param agent
     */
    @Override
    @Transactional
    public void createOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder, Agent agent,String payPassword) {
        if(organizationRechargeOrder == null || StringUtils.isEmpty(organizationRechargeOrder.getOrganizationCode())  || organizationRechargeOrder.getRechargeAmount() < 0){
            throw new FebsException("账号信息不完整");
        }
        if(StringUtils.isEmpty(agent.getGoogleKey())){
            if(!agent.getPayPassword().equals(MD5Util.MD5(payPassword))){
                throw new FebsException("支付密码错误");
            }
        }
        Organization Organization = this.organizationService.queryOrgByCodeForUpdate(organizationRechargeOrder.getOrganizationCode());
        if(Organization == null || !Organization.getDataMd5().equals(organizationService.getDataMd5(Organization)) || !Organization.getAgentId().equals(agent.getId())){
            throw new FebsException("商户信息错误");
        }
        Agent updataAgent = agentService.queryOrgByIdForUpdate(agent.getId());
        if(!updataAgent.getDataMd5().equals(agentService.getDataMd5(updataAgent))){
            throw new FebsException("代理信息错误");
        }
        if(organizationRechargeOrder.getRechargeAmount() > updataAgent.getQuotaAmount()){
            throw new FebsException("代理可用额度不足");
        }
        organizationRechargeOrder.setCreateTime(new Date());
        organizationRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        organizationRechargeOrder.setPayChannelId("");
        organizationRechargeOrder.setPayMerchNo("");
        organizationRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","代理充值").getCode());
        organizationRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setAvailableAmount(new Long(Organization.getAvailableAmount()).intValue() + organizationRechargeOrder.getRechargeAmount());
        organizationRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        organizationRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        organizationRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setRecordedType(RecordedTypeEnums.Booked.getCode());
        this.save(organizationRechargeOrder);
        updataAgent.setQuotaAmount(updataAgent.getQuotaAmount() - organizationRechargeOrder.getRechargeAmount());
        updataAgent.setCashAmount(updataAgent.getCashAmount() + organizationRechargeOrder.getRechargeAmount());
        updataAgent.setDataMd5(agentService.getDataMd5(updataAgent));
        agentService.updateById(updataAgent);
        Organization.setAmount(Organization.getAmount() + organizationRechargeOrder.getRechargeAmount());
        Organization.setAvailableAmount(Organization.getAvailableAmount() + organizationRechargeOrder.getRechargeAmount());
        Organization.setDataMd5(organizationService.getDataMd5(Organization));
        organizationService.updateById(Organization);
    }
    @Override
    @Transactional
    public void updateOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder) {
        this.saveOrUpdate(organizationRechargeOrder);
    }

    @Override
    @Transactional
    public void deleteOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder) {
        LambdaQueryWrapper<OrganizationRechargeOrder> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationRechargeOrders(String[] organizationRechargeOrderIds) {
        List<String> list = Arrays.asList(organizationRechargeOrderIds);
        this.removeByIds(list);
    }
}
