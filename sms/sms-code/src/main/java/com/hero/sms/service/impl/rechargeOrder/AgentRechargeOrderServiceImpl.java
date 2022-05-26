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
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import com.hero.sms.enums.common.RecordedTypeEnums;
import com.hero.sms.mapper.rechargeOrder.AgentRechargeOrderMapper;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.rechargeOrder.IAgentRechargeOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 代理充值订单 Service实现
 *
 * @author Administrator
 * @date 2020-03-12 18:00:59
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentRechargeOrderServiceImpl extends ServiceImpl<AgentRechargeOrderMapper, AgentRechargeOrder> implements IAgentRechargeOrderService {

    @Autowired
    private AgentRechargeOrderMapper agentRechargeOrderMapper;
    @Autowired
    private IAgentService agentService;

    @Override
    public IPage<AgentRechargeOrder> findAgentRechargeOrders(QueryRequest request, AgentRechargeOrder agentRechargeOrder,Date agentReqStartTime, Date agentReqEndTime) {
        LambdaQueryWrapper<AgentRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(agentRechargeOrder.getAgentId())){
            queryWrapper.eq(AgentRechargeOrder::getAgentId,agentRechargeOrder.getAgentId());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getNetwayCode())){
            queryWrapper.eq(AgentRechargeOrder::getNetwayCode,agentRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getOrderNo())){
            queryWrapper.eq(AgentRechargeOrder::getOrderNo,agentRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getReqStateCode())){
            queryWrapper.eq(AgentRechargeOrder::getReqStateCode,agentRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeType())){
            queryWrapper.eq(AgentRechargeOrder::getRechargeType,agentRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq(AgentRechargeOrder::getRechargeStateCode,agentRechargeOrder.getRechargeStateCode());
        }
        if(agentReqStartTime != null){
            queryWrapper.ge(AgentRechargeOrder::getReqTime,agentReqStartTime);
        }else{
            queryWrapper.ge(AgentRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(agentReqEndTime != null){
            queryWrapper.le(AgentRechargeOrder::getReqTime,agentReqEndTime);
        }else {
            queryWrapper.le(AgentRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        /**
         * 2021-04-07
         * 新增入账方式条件
         */
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRecordedType())){
            queryWrapper.eq(AgentRechargeOrder::getRecordedType,agentRechargeOrder.getRecordedType());
        }
        queryWrapper.orderByDesc(AgentRechargeOrder::getId);
        Page<AgentRechargeOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    /**
     * 根据代理查询代理充值订单
     * @param request
     * @param agentRechargeOrder
     * @param agentReqStartTime
     * @param agentReqEndTime
     * @param upAgent
     * @return
     */
    @Override
    public IPage<AgentRechargeOrder> findAgentRechargeOrders(QueryRequest request, AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime, Agent upAgent) {
        QueryWrapper<AgentRechargeOrder> queryWrapper = new QueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(agentRechargeOrder.getAgentId())){
            queryWrapper.eq("o.`agent_id`",agentRechargeOrder.getAgentId());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getNetwayCode())){
            queryWrapper.eq("o.`netway_code`",agentRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getOrderNo())){
            queryWrapper.eq("o.`order_no`",agentRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getReqStateCode())){
            queryWrapper.eq("o.`req_state_code`",agentRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeType())){
            queryWrapper.eq("o.`recharge_type`",agentRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq("o.`recharge_state_code`",agentRechargeOrder.getRechargeStateCode());
        }
        if(agentReqStartTime != null){
            queryWrapper.ge("o.`req_time`",agentReqStartTime);
        }else{
            queryWrapper.ge("o.`req_time`",DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(agentReqEndTime != null){
            queryWrapper.le("o.`req_time`",agentReqEndTime);
        }else {
            queryWrapper.le("o.`req_time`",DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        queryWrapper.or();
        queryWrapper.eq("a.`Up_Agent_Id`",upAgent.getId());
        if(StringUtils.isNotEmpty(agentRechargeOrder.getNetwayCode())){
            queryWrapper.eq("o.`netway_code`",agentRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getOrderNo())){
            queryWrapper.eq("o.`order_no`",agentRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getReqStateCode())){
            queryWrapper.eq("o.`req_state_code`",agentRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeType())){
            queryWrapper.eq("o.`recharge_type`",agentRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq("o.`recharge_state_code`",agentRechargeOrder.getRechargeStateCode());
        }
        if(agentReqStartTime != null){
            queryWrapper.ge("o.`req_time`",agentReqStartTime);
        }else{
            queryWrapper.ge("o.`req_time`",DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(agentReqEndTime != null){
            queryWrapper.le("o.`req_time`",agentReqEndTime);
        }else {
            queryWrapper.le("o.`req_time`",DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        /**
         * 2021-04-07
         * 新增入账方式条件
         */
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRecordedType())){
            queryWrapper.eq("o.`recorded_type`",agentRechargeOrder.getRecordedType());
        }
        queryWrapper.orderByDesc("o.`Id`");
        Page<AgentRechargeOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.agentRechargeOrderMapper.selectByAgentRechargeOrderPage(page, queryWrapper);
    }

    @Override
    public List<AgentRechargeOrder> findAgentRechargeOrders(AgentRechargeOrder agentRechargeOrder) {
	    LambdaQueryWrapper<AgentRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }


    /**
     * 代理端充值
     * @param agentRechargeOrder
     * @param upAgent
     * @param payPassword
     */
    @Override
    @Transactional
    public void createAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder, Agent upAgent, String payPassword) {
        if(agentRechargeOrder == null || StringUtils.isEmpty(agentRechargeOrder.getAgentId()) || agentRechargeOrder.getRechargeAmount() < 0){
            throw new FebsException("账号信息不完整");
        }
        if(StringUtils.isEmpty(upAgent.getGoogleKey())){
            if(!upAgent.getPayPassword().equals(MD5Util.MD5(payPassword))){
                throw new FebsException("支付密码错误");
            }
        }
        Agent agent = this.agentService.queryOrgByIdForUpdate( Integer.parseInt(agentRechargeOrder.getAgentId()));
        if(agent == null || !agent.getDataMd5().equals(agentService.getDataMd5(agent))){
            throw new FebsException("下级代理信息错误");
        }
        if(!agent.getUpAgentId().equals(upAgent.getId())){
            throw new FebsException("无权限为该代理充值");
        }
        Agent updataAgent = agentService.queryOrgByIdForUpdate(upAgent.getId());
        if(!updataAgent.getDataMd5().equals(agentService.getDataMd5(updataAgent))){
            throw new FebsException("上级代理信息错误");
        }
        if(agentRechargeOrder.getRechargeAmount() > updataAgent.getQuotaAmount()){
            throw new FebsException("代理可用额度不足");
        }
        agentRechargeOrder.setCreateTime(new Date());
        agentRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        agentRechargeOrder.setPayChannelId("");
        agentRechargeOrder.setPayMerchNo("");
        agentRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","代理充值").getCode());
        agentRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setAvailableAmount(new Long(agent.getQuotaAmount()).intValue() + agentRechargeOrder.getRechargeAmount());
        agentRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        agentRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        agentRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRecordedType(RecordedTypeEnums.Booked.getCode());
        this.save(agentRechargeOrder);
        updataAgent.setQuotaAmount(updataAgent.getQuotaAmount() - agentRechargeOrder.getRechargeAmount());
        updataAgent.setCashAmount(updataAgent.getCashAmount() + agentRechargeOrder.getRechargeAmount());
        updataAgent.setDataMd5(agentService.getDataMd5(updataAgent));
        agentService.updateById(updataAgent);
        agent.setAmount(agent.getAmount() + agentRechargeOrder.getRechargeAmount());
        agent.setQuotaAmount(agent.getQuotaAmount() + agentRechargeOrder.getRechargeAmount());
        agent.setDataMd5(agentService.getDataMd5(agent));
        agentService.updateById(agent);
    }

    @Override
    @Transactional
    public void createAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder) {
        if(agentRechargeOrder == null || StringUtils.isEmpty(agentRechargeOrder.getAgentId()) || agentRechargeOrder.getRechargeAmount() < 0){
            throw new FebsException("账号信息不完整");
        }
        Agent agent = this.agentService.queryOrgByIdForUpdate( Integer.parseInt(agentRechargeOrder.getAgentId()));
        if(agent == null || !agent.getDataMd5().equals(agentService.getDataMd5(agent))){
            throw new FebsException("代理信息错误");
        }
        agentRechargeOrder.setCreateTime(new Date());
        agentRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        agentRechargeOrder.setPayChannelId("");
        agentRechargeOrder.setPayMerchNo("");
        agentRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","平台充值").getCode());
        agentRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setAvailableAmount(new Long(agent.getQuotaAmount()).intValue() + agentRechargeOrder.getRechargeAmount());
        agentRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        agentRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        agentRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRecordedType(RecordedTypeEnums.Booked.getCode());
        this.save(agentRechargeOrder);
        agent.setAmount(agent.getAmount() + agentRechargeOrder.getRechargeAmount());
        agent.setQuotaAmount(agent.getQuotaAmount() + agentRechargeOrder.getRechargeAmount());
        agent.setDataMd5(agentService.getDataMd5(agent));
        agentService.updateById(agent);
    }

    /**
     * 扣除额度
     * @param agentRechargeOrder
     */
    @Override
    @Transactional
    public void deducting(AgentRechargeOrder agentRechargeOrder) {
        if(agentRechargeOrder == null || StringUtils.isEmpty(agentRechargeOrder.getAgentId()) || agentRechargeOrder.getRechargeAmount() < 0){
            throw new FebsException("账号信息不完整");
        }
        Agent agent = this.agentService.queryOrgByIdForUpdate( Integer.parseInt(agentRechargeOrder.getAgentId()));
        if(agent == null || !agent.getDataMd5().equals(agentService.getDataMd5(agent))){
            throw new FebsException("代理信息错误");
        }
        agentRechargeOrder.setCreateTime(new Date());
        agentRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        agentRechargeOrder.setPayChannelId("");
        agentRechargeOrder.setPayMerchNo("");
        agentRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","平台充值").getCode());
        agentRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setAvailableAmount(new Long(agent.getQuotaAmount()).intValue() - agentRechargeOrder.getRechargeAmount());
        agentRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        agentRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
        agentRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRecordedType(RecordedTypeEnums.Booked.getCode());
        this.save(agentRechargeOrder);
        agent.setAmount(agent.getAmount() - agentRechargeOrder.getRechargeAmount());
        agent.setQuotaAmount(agent.getQuotaAmount() - agentRechargeOrder.getRechargeAmount());
        agent.setDataMd5(agentService.getDataMd5(agent));
        agentService.updateById(agent);
    }

    @Override
    @Transactional
    public void updateAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder) {
        this.saveOrUpdate(agentRechargeOrder);
    }

    @Override
    @Transactional
    public void deleteAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder) {
        LambdaQueryWrapper<AgentRechargeOrder> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteAgentRechargeOrders(String[] agentRechargeOrderIds) {
        List<String> list = Arrays.asList(agentRechargeOrderIds);
        this.removeByIds(list);
    }

    /**
     * 代理充值订单统计
     * @param agentRechargeOrder
     * @param agentReqStartTime
     * @param agentReqEndTime
     * @return
     */
    @Override
    public List<StatisticBean> statisticBeanList(AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime) {
        LambdaQueryWrapper<AgentRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(agentRechargeOrder.getAgentId())){
            queryWrapper.eq(AgentRechargeOrder::getAgentId,agentRechargeOrder.getAgentId());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getNetwayCode())){
            queryWrapper.eq(AgentRechargeOrder::getNetwayCode,agentRechargeOrder.getNetwayCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getOrderNo())){
            queryWrapper.eq(AgentRechargeOrder::getOrderNo,agentRechargeOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getReqStateCode())){
            queryWrapper.eq(AgentRechargeOrder::getReqStateCode,agentRechargeOrder.getReqStateCode());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeType())){
            queryWrapper.eq(AgentRechargeOrder::getRechargeType,agentRechargeOrder.getRechargeType());
        }
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRechargeStateCode())){
            queryWrapper.eq(AgentRechargeOrder::getRechargeStateCode,agentRechargeOrder.getRechargeStateCode());
        }
        if(agentReqStartTime != null){
            queryWrapper.ge(AgentRechargeOrder::getReqTime,agentReqStartTime);
        }else{
            queryWrapper.ge(AgentRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(agentReqEndTime != null){
            queryWrapper.le(AgentRechargeOrder::getReqTime,agentReqEndTime);
        }else {
            queryWrapper.le(AgentRechargeOrder::getReqTime,DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        /**
         * 2021-04-07
         * 新增入账方式条件
         */
        if(StringUtils.isNotEmpty(agentRechargeOrder.getRecordedType())){
            queryWrapper.eq(AgentRechargeOrder::getRecordedType,agentRechargeOrder.getRecordedType());
        }
        queryWrapper.groupBy(AgentRechargeOrder::getNetwayCode);
        return this.baseMapper.statisticRechargeAmount(queryWrapper);
    }

}
