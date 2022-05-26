package com.hero.sms.service.impl.agentRemitOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agentRemitOrder.AgentRemitOrder;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.mapper.agentRemitOrder.AgentRemitOrderMapper;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.agentRemitOrder.IAgentRemitOrderService;
import com.hero.sms.service.rechargeOrder.IAgentRechargeOrderService;
import com.hero.sms.utils.RandomUtil;

/**
 * 代理提现订单 Service实现
 *
 * @author Administrator
 * @date 2020-04-02 22:24:19
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentRemitOrderServiceImpl extends ServiceImpl<AgentRemitOrderMapper, AgentRemitOrder> implements IAgentRemitOrderService {

    @Autowired
    private AgentRemitOrderMapper agentRemitOrderMapper;
    @Autowired
    private IAgentService agentService;
    @Autowired
    private IAgentRechargeOrderService agentRechargeOrderService;

    public static final String Md5Key = "IJIDjldf4465f46w464gh46axc5s25W";

    @Override
    public IPage<AgentRemitOrder> findAgentRemitOrders(QueryRequest request, AgentRemitOrder agentRemitOrder, Date createStartTime, Date createEndTime) {
        LambdaQueryWrapper<AgentRemitOrder> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(agentRemitOrder.getAgentId())){
            queryWrapper.eq(AgentRemitOrder::getAgentId,agentRemitOrder.getAgentId());
        }
        if(StringUtils.isNotEmpty(agentRemitOrder.getOrderNo())){
            queryWrapper.like(AgentRemitOrder::getOrderNo,agentRemitOrder.getOrderNo());
        }
        if(StringUtils.isNotEmpty(agentRemitOrder.getBankCode())){
            queryWrapper.eq(AgentRemitOrder::getBankCode,agentRemitOrder.getBankCode());
        }
        if(StringUtils.isNotEmpty(agentRemitOrder.getBankAccountNo())){
            queryWrapper.eq(AgentRemitOrder::getBankAccountNo,agentRemitOrder.getBankAccountNo());
        }
        if(agentRemitOrder.getRemitAmount() != null){
            queryWrapper.eq(AgentRemitOrder::getRemitAmount,agentRemitOrder.getRemitAmount());
        }
        if(StringUtils.isNotEmpty(agentRemitOrder.getStatus())){
            queryWrapper.eq(AgentRemitOrder::getStatus,agentRemitOrder.getStatus());
        }
        if(createStartTime != null){
            queryWrapper.ge(AgentRemitOrder::getCreateTime,createStartTime);
        }else{
            queryWrapper.ge(AgentRemitOrder::getCreateTime,DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if(createEndTime != null){
            queryWrapper.le(AgentRemitOrder::getCreateTime,createEndTime);
        }else {
            queryWrapper.le(AgentRemitOrder::getCreateTime, DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        queryWrapper.orderByDesc(AgentRemitOrder::getId);
        Page<AgentRemitOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentRemitOrder> findAgentRemitOrders(AgentRemitOrder agentRemitOrder) {
	    LambdaQueryWrapper<AgentRemitOrder> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createAgentRemitOrder(AgentRemitOrder agentRemitOrder,String payPassword) {
        if(agentRemitOrder == null || StringUtils.isEmpty(agentRemitOrder.getBankCode())
                || StringUtils.isEmpty(agentRemitOrder.getBankAccountNo()) || StringUtils.isEmpty(agentRemitOrder.getBankAccountName())
                || StringUtils.isEmpty(agentRemitOrder.getBankBranch()) || agentRemitOrder.getRemitAmount() == null){
            throw new FebsException("提现信息不完整");
        }
        if(agentRemitOrder.getRemitAmount() < 0){
            throw new FebsException("提现金额错误！");
        }
        Integer agentId = Integer.parseInt(agentRemitOrder.getAgentId());
        Agent agent = this.agentService.queryOrgByIdForUpdate(agentId);
        if(agent == null){
            throw new FebsException("该代理不存在！");
        }
        if(StringUtils.isEmpty(agent.getGoogleKey())){
            if(!agent.getPayPassword().equals(MD5Util.MD5(payPassword))){
                throw new FebsException("支付密码错误");
            }
        }
        if(!agent.getDataMd5().equals(agentService.getDataMd5(agent))){
            throw new FebsException("该代理信息异常！");
        }
        if(agent.getAvailableAmount() < agentRemitOrder.getRemitAmount()){
            throw new FebsException("可用利润不足！");
        }
        agentRemitOrder.setAvailableAmount(new Long(agent.getAvailableAmount() - agentRemitOrder.getRemitAmount()).intValue());
        agentRemitOrder.setOrderNo(DateUtil.getString(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomUtil.randomStr(5));
        agentRemitOrder.setStatus(AuditStateEnums.Wait.getCode() + "");
        agentRemitOrder.setCeateUser(agent.getAgentName());
        agentRemitOrder.setCreateTime(new Date());
        agentRemitOrder.setDataMd5(getDataMd5(agentRemitOrder));
        this.save(agentRemitOrder);
        agent.setAvailableAmount(agent.getAvailableAmount() - agentRemitOrder.getRemitAmount());
        agent.setCashAmount(agent.getCashAmount() + agentRemitOrder.getRemitAmount());
        Agent newAgent = new Agent();
        newAgent.setId(agent.getId());
        newAgent.setAvailableAmount(agent.getAvailableAmount());
        newAgent.setCashAmount(agent.getCashAmount());
        newAgent.setDataMd5(this.agentService.getDataMd5(agent));
        this.agentService.updateById(newAgent);
    }

    @Override
    @Transactional
    public void updateAgentRemitOrder(AgentRemitOrder agentRemitOrder) {
        this.saveOrUpdate(agentRemitOrder);
    }

    @Override
    @Transactional
    public void deleteAgentRemitOrder(AgentRemitOrder agentRemitOrder) {
        LambdaQueryWrapper<AgentRemitOrder> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteAgentRemitOrders(String[] agentRemitOrderIds) {
        List<String> list = Arrays.asList(agentRemitOrderIds);
        this.removeByIds(list);
    }

    @Override
    public String getDataMd5(AgentRemitOrder agentRemitOrder){
        StringBuffer str = new StringBuffer();
        str.append(Md5Key);
        str.append(agentRemitOrder.getAgentId());
        str.append(agentRemitOrder.getOrderNo());
        str.append(agentRemitOrder.getBankCode());
        str.append(agentRemitOrder.getBankAccountName());
        str.append(agentRemitOrder.getBankAccountNo());
        str.append(agentRemitOrder.getBankBranch());
        str.append(agentRemitOrder.getRemitAmount());
        str.append(agentRemitOrder.getAvailableAmount());
        str.append(agentRemitOrder.getStatus());
        return MD5Util.MD5(str.toString());
    }
    @Override
    public boolean checkDataMd5(AgentRemitOrder agentRemitOrder){
        LambdaQueryWrapper<AgentRemitOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentRemitOrder::getId,agentRemitOrder.getId());
        AgentRemitOrder oldAgentRemitOrder = this.baseMapper.selectOne(queryWrapper);
        if(oldAgentRemitOrder != null && oldAgentRemitOrder.getDataMd5().equals(getDataMd5(agentRemitOrder))){
            return true;
        }
        return false;
    }

    /**
     * 审核通过
     * @param agentRemitOrder
     */
    @Override
    @Transactional
    public void auditSuccess(AgentRemitOrder agentRemitOrder){
        if (agentRemitOrder == null || agentRemitOrder.getId() == null ) {
            throw new FebsException("该订单不存在！");
        }
        AgentRemitOrder order = getAgentRemitOrderByIdForUpdate(agentRemitOrder.getId());
        if (order.getStatus().equals(AuditStateEnums.NoPass.getCode() + "")) {
            throw new FebsException("当前状态不能进行此操作！");
        }
        if(!order.getDataMd5().equals(getDataMd5(order))){
            throw new FebsException("该订单信息异常！");
        }
        try {
            AgentRemitOrder newAgentRemitOrder = new AgentRemitOrder();
            if (agentRemitOrder.getRemitPictureFile() != null) {
                Code path = DatabaseCache.getCodeBySortCodeAndCode("System", "logoFilePath");
                Code size = DatabaseCache.getCodeBySortCodeAndCode("System", "pictureSize");
                StringBuffer imgPath = new StringBuffer();
                imgPath.append("/agentRemitOrder/").append(order.getOrderNo()).append(".png");
                FileUtil.savePpictureFile(agentRemitOrder.getRemitPictureFile(), path.getName() + imgPath.toString(), Long.parseLong(size.getName()));
                newAgentRemitOrder.setRemitPictureUrl("/res" + imgPath.toString());
            }
            order.setStatus(AuditStateEnums.Pass.getCode() + "");
            newAgentRemitOrder.setId(order.getId());
            newAgentRemitOrder.setStatus(AuditStateEnums.Pass.getCode() + "");
            newAgentRemitOrder.setApproveTime(new Date());
            newAgentRemitOrder.setDataMd5(getDataMd5(order));
            this.updateById(newAgentRemitOrder);
        }catch (Exception e){
            throw new FebsException(e.getMessage());
        }
    }

    /**
     * 审核不通过
     * @param id
     */
    @Override
    @Transactional
    public void auditFail(Integer id) {
        AgentRemitOrder order = getAgentRemitOrderByIdForUpdate(id);
        if (!order.getStatus().equals(AuditStateEnums.Wait.getCode() + "")) {
            throw new FebsException("当前状态不能进行此操作！");
        }
        if(!order.getDataMd5().equals(getDataMd5(order))){
            throw new FebsException("该订单信息异常！");
        }
        Integer agentId = Integer.parseInt(order.getAgentId());
        Agent agent = this.agentService.queryOrgByIdForUpdate(agentId);
        if(agent == null){
            throw new FebsException("该代理不存在！");
        }
        if(!agent.getDataMd5().equals(agentService.getDataMd5(agent))){
            throw new FebsException("该代理信息异常！");
        }
        AgentRemitOrder newAgentRemitOrder = new AgentRemitOrder();
        order.setStatus(AuditStateEnums.NoPass.getCode() + "");
        newAgentRemitOrder.setId(order.getId());
        newAgentRemitOrder.setStatus(AuditStateEnums.NoPass.getCode() + "");
        newAgentRemitOrder.setApproveTime(new Date());
        newAgentRemitOrder.setDataMd5(getDataMd5(order));
        newAgentRemitOrder.setRemark("退款后可用利润：" + (agent.getAvailableAmount() + order.getRemitAmount()));
        this.updateById(newAgentRemitOrder);
        agent.setAvailableAmount(agent.getAvailableAmount() + order.getRemitAmount());
        agent.setCashAmount(agent.getCashAmount() - order.getRemitAmount());
        Agent newAgent = new Agent();
        newAgent.setId(agent.getId());
        newAgent.setAvailableAmount(agent.getAvailableAmount());
        newAgent.setCashAmount(agent.getCashAmount());
        newAgent.setDataMd5(this.agentService.getDataMd5(agent));
        this.agentService.updateById(newAgent);
    }

    private AgentRemitOrder getAgentRemitOrderByIdForUpdate(Integer id){
        LambdaQueryWrapper<AgentRemitOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentRemitOrder::getId,id).last("FOR UPDATE");
        return this.getOne(wrapper);
    }

    /**
     * 获取银行类型缓存
     * @return
     */
    @Override
    public List<Map<String,String>> getBanks(){
        List<Map<String,String>> list = new ArrayList<>();
        for (Code code: DatabaseCache.getCodeListBySortCode("Bank")) {
            Map<String,String> map = new HashMap<>();
            map.put(code.getCode(),code.getName());
            list.add(map);
        }
        return list;
    }

    /**
     * 代理利润转额度
     * @param agent
     * @param remitAmount
     * @param payPassword
     */
    @Override
    @Transactional
    public void transfer(Agent agent,Integer remitAmount, String payPassword){
        if(remitAmount == null || remitAmount < 0){
            throw new FebsException("提现金额错误！");
        }
        Agent oldAgent = this.agentService.queryOrgByIdForUpdate(agent.getId());
        if(StringUtils.isEmpty(oldAgent.getGoogleKey())){
            if(!oldAgent.getPayPassword().equals(MD5Util.MD5(payPassword))){
                throw new FebsException("支付密码错误");
            }
        }
        if(oldAgent.getAvailableAmount() < remitAmount){
            throw new FebsException("可用利润不足！");
        }
        Agent newAgent = new Agent();
        newAgent.setId(oldAgent.getId());
        newAgent.setAmount(oldAgent.getAmount() + remitAmount);
        newAgent.setQuotaAmount(oldAgent.getQuotaAmount() + remitAmount);
        newAgent.setAvailableAmount(oldAgent.getAvailableAmount() - remitAmount);
        newAgent.setCashAmount(oldAgent.getCashAmount() + remitAmount);
        oldAgent.setAmount(oldAgent.getAmount() + remitAmount);
        oldAgent.setQuotaAmount(oldAgent.getQuotaAmount() + remitAmount);
        oldAgent.setAvailableAmount(oldAgent.getAvailableAmount() - remitAmount);
        oldAgent.setCashAmount(oldAgent.getCashAmount() + remitAmount);
        newAgent.setDataMd5(this.agentService.getDataMd5(oldAgent));
        String orderNo = DateUtil.getString(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomUtil.randomStr(5);
        AgentRemitOrder agentRemitOrder = new AgentRemitOrder();
        agentRemitOrder.setAgentId(oldAgent.getId() + "");
        agentRemitOrder.setRemitAmount(remitAmount);
        agentRemitOrder.setAvailableAmount(new Long(oldAgent.getAvailableAmount()).intValue());
        agentRemitOrder.setOrderNo(orderNo);
        agentRemitOrder.setStatus(AuditStateEnums.Pass.getCode() + "");
        agentRemitOrder.setApproveTime(new Date());
        agentRemitOrder.setCeateUser(oldAgent.getAgentName());
        agentRemitOrder.setCreateTime(new Date());
        agentRemitOrder.setRemark("利润转额度");
        agentRemitOrder.setDataMd5(getDataMd5(agentRemitOrder));
        AgentRechargeOrder agentRechargeOrder = new AgentRechargeOrder();
        agentRechargeOrder.setAgentId(oldAgent.getId() + "");
        agentRechargeOrder.setOrderNo(orderNo);
        agentRechargeOrder.setRechargeType("agentRecharge");
        agentRechargeOrder.setNetwayCode("TRANSFER");
        agentRechargeOrder.setRechargeAmount(remitAmount);
        agentRechargeOrder.setAvailableAmount(new Long(oldAgent.getQuotaAmount()).intValue());
        agentRechargeOrder.setReqStateCode("00");
        agentRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRechargeStateCode("00");
        agentRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRemark("利润转额度");
        agentRechargeOrder.setCeateUser(oldAgent.getAgentName());
        agentRechargeOrder.setCreateTime(new Date());
        this.agentService.updateById(newAgent);
        this.save(agentRemitOrder);
        this.agentRechargeOrderService.save(agentRechargeOrder);
    }

}
