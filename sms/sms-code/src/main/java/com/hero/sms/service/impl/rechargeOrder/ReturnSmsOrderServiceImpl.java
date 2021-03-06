package com.hero.sms.service.impl.rechargeOrder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.RandomStringUtil;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.rechargeOrder.IAgentRechargeOrderService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrder;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrderQuery;
import com.hero.sms.mapper.rechargeOrder.ReturnSmsOrderMapper;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.impl.agent.AgentServiceImpl;
import com.hero.sms.service.impl.organization.OrganizationServiceImpl;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.rechargeOrder.IReturnSmsOrderService;
import com.hero.sms.utils.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ???????????????????????? Service??????
 *
 * @author Administrator
 * @date 2020-04-20 00:13:55
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ReturnSmsOrderServiceImpl extends ServiceImpl<ReturnSmsOrderMapper, ReturnSmsOrder> implements IReturnSmsOrderService {

    @Autowired
    private ReturnSmsOrderMapper returnSmsOrderMapper;

    @Autowired
    private IAgentService agentService;

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private ISendBoxService sendBoxService;

    @Autowired
    private IAgentRechargeOrderService agentRechargeOrderService;

    @Autowired
    private IOrganizationRechargeOrderService organizationRechargeOrderService;

    @Override
    public IPage<ReturnSmsOrder> findReturnSmsOrders(QueryRequest request, ReturnSmsOrderQuery returnSmsOrder) {
        LambdaQueryWrapper<ReturnSmsOrder> queryWrapper = new LambdaQueryWrapper<>();
        // TODO ??????????????????
        if (StringUtils.isNotBlank(returnSmsOrder.getOrderNo())){//?????????
            queryWrapper.eq(ReturnSmsOrder::getOrderNo,returnSmsOrder.getOrderNo());
        }
        if (StringUtils.isNotBlank(returnSmsOrder.getOrgCode())){
            queryWrapper.eq(ReturnSmsOrder::getOrgCode,returnSmsOrder.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnSmsOrder.getSendCode())){
            queryWrapper.eq(ReturnSmsOrder::getSendCode,returnSmsOrder.getSendCode());
        }
        if (returnSmsOrder.getAgentId() != null){
            queryWrapper.eq(ReturnSmsOrder::getAgentId,returnSmsOrder.getAgentId());
        }
        if (returnSmsOrder.getCreateStartTime() != null){
            queryWrapper.ge(ReturnSmsOrder::getCreateTime,returnSmsOrder.getCreateStartTime());
        }else {
        	queryWrapper.ge(ReturnSmsOrder::getCreateTime,DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        }
        if (returnSmsOrder.getCreateEndTime() != null){
            queryWrapper.le(ReturnSmsOrder::getCreateTime,returnSmsOrder.getCreateEndTime());
        }else {
        	queryWrapper.le(ReturnSmsOrder::getCreateTime,DateUtil.getString(new Date(),"yyyy-MM-dd 23:59:59"));
        }
        if (returnSmsOrder.getAgentIdQueryOr() != null){
            queryWrapper.and(i ->
                i.eq(ReturnSmsOrder::getAgentId,returnSmsOrder.getAgentIdQueryOr())
                 .or()
                 .eq(ReturnSmsOrder::getUpAgentId,returnSmsOrder.getAgentIdQueryOr())
            );
        }
        queryWrapper.orderByDesc(ReturnSmsOrder::getCreateTime);
        Page<ReturnSmsOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<ReturnSmsOrder> findReturnSmsOrders(ReturnSmsOrderQuery returnSmsOrder) {
	    LambdaQueryWrapper<ReturnSmsOrder> queryWrapper = new LambdaQueryWrapper<>();
		// TODO ??????????????????
        if (StringUtils.isNotBlank(returnSmsOrder.getOrderNo())){//?????????
            queryWrapper.eq(ReturnSmsOrder::getOrderNo,returnSmsOrder.getOrderNo());
        }
        if (StringUtils.isNotBlank(returnSmsOrder.getOrgCode())){
            queryWrapper.eq(ReturnSmsOrder::getOrgCode,returnSmsOrder.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnSmsOrder.getSendCode())){
            queryWrapper.eq(ReturnSmsOrder::getSendCode,returnSmsOrder.getSendCode());
        }
        if (returnSmsOrder.getAgentId() != null){
            queryWrapper.eq(ReturnSmsOrder::getAgentId,returnSmsOrder.getAgentId());
        }
        if (returnSmsOrder.getCreateStartTime() != null){
            queryWrapper.ge(ReturnSmsOrder::getCreateTime,returnSmsOrder.getCreateStartTime());
        }
        if (returnSmsOrder.getCreateEndTime() != null){
            queryWrapper.le(ReturnSmsOrder::getCreateTime,returnSmsOrder.getCreateEndTime());
        }
        queryWrapper.orderByDesc(ReturnSmsOrder::getCreateTime);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Async
    public void createTask(List<ReturnSmsOrder> data, String username) {
        data.forEach(item -> {
            try {
                this.createReturnSmsOrder(item,username);
            } catch (ServiceException e) {
                log.error(String.format("????????????%s??????????????????????????????:%s",item.getSendCode(),e.getMessage()));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReturnSmsOrder(ReturnSmsOrder returnSmsOrder,String createUser) throws ServiceException {
        if (StringUtils.isBlank(returnSmsOrder.getSendCode())){
            throw new ServiceException("????????????????????????");
        }
        if (returnSmsOrder.getSmsNum() == null || returnSmsOrder.getSmsNum() < 1){
            throw new ServiceException("????????????????????????????????????1");
        }
        LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseSend::getSendCode,returnSmsOrder.getSendCode());
        SendBox sendbox = null;
        try {
            sendbox = sendBoxService.getOne(queryWrapper);
        } catch (Exception e) {
            log.error("???????????????????????????????????????????????????",e);
            throw new ServiceException("???????????????????????????????????????????????????");
        }

        Integer smsCount = sendbox.getSmsCount();
        if (smsCount < 1){
            throw new ServiceException("???????????????????????????????????????????????????");
        }
        if (returnSmsOrder.getSmsNum() > smsCount ){
            throw new ServiceException(String.format("???????????????????????????%s???",smsCount));
        }

        //???????????????  ??????????????? ???????????????/????????????/??????
        Integer consumeAmount = sendbox.getConsumeAmount();
        Integer agentIncomeAmount= sendbox.getAgentIncomeAmount();
        Integer upAgentIncomeAmount = sendbox.getUpAgentIncomeAmount();
        Integer orgIncomeUnit = 0;
        Integer agentIncomeUnit = 0;
        Integer upAgentIncomeUnit = 0;
        if (consumeAmount != null && consumeAmount > 0){
            orgIncomeUnit = consumeAmount/smsCount;
        }
        if (agentIncomeAmount != null && agentIncomeAmount > 0){
            agentIncomeUnit = agentIncomeAmount/smsCount;
        }
        if (upAgentIncomeAmount != null && upAgentIncomeAmount > 0){
            upAgentIncomeUnit = upAgentIncomeAmount/smsCount;
        }

        //??????????????????
        Integer orgReturnAmount = orgIncomeUnit * returnSmsOrder.getSmsNum();
        Integer agentReturnAmount = agentIncomeUnit * returnSmsOrder.getSmsNum();
        Integer upAgentReturnAmount = upAgentIncomeUnit * returnSmsOrder.getSmsNum();

        //?????????????????????????????????
        returnSmsOrder.setCreateTime(new Date());
        returnSmsOrder.setOrgCode(sendbox.getOrgCode());
        returnSmsOrder.setAgentId(sendbox.getAgentId());
        if (sendbox.getUpAgentId() != null){
            returnSmsOrder.setUpAgentId(sendbox.getUpAgentId());
        }
        returnSmsOrder.setOrgReturnAmount(orgReturnAmount);
        returnSmsOrder.setAgentReturnAmount(agentReturnAmount);
        returnSmsOrder.setUpAgentReturnAmount(upAgentReturnAmount);
        returnSmsOrder.setOrderNo(RandomUtil.randomStringWithDate(6));
        //??????
        this.save(returnSmsOrder);

        //??????????????????
        updateOrgAmout(returnSmsOrder,createUser);
        //??????????????????
        updataAgentAmout(returnSmsOrder,createUser);

    }

    /**
     * ??????????????????
     * @param returnSmsOrder
     * @throws ServiceException
     */
    public void updataAgentAmout(ReturnSmsOrder returnSmsOrder,String createUser) throws ServiceException{
        Integer agentReturnAmount = returnSmsOrder.getAgentReturnAmount();

        if(agentReturnAmount<0) {
            String errMsg = String.format("?????????%d????????????????????????%f?????????",returnSmsOrder.getAgentId(),agentReturnAmount/100);
            throw new ServiceException(errMsg);
        }
        Integer agentId = returnSmsOrder.getAgentId();
        if(agentId == null) {
            throw new ServiceException("??????id???????????????");
        }
        Agent agent = agentService.queryOrgByIdForUpdate(agentId);
        if(agent == null) {
            String errMsg = String.format("?????????%d????????????",agentId);
            throw new ServiceException(errMsg);
        }

        String agentMd5Data = agentService.getDataMd5(agent);
        if (!agent.getDataMd5().equals(agentMd5Data)){
            String errMsg = String.format("?????????%d???????????????????????????",agentId);
            throw new ServiceException(errMsg);
        }

        if(agentReturnAmount > 0) {
            //??????????????????????????????
            int availableAmount = agent.getAvailableAmount().intValue() - agentReturnAmount;
            savaAgentRechargeOrder(agentReturnAmount,String.valueOf(agentId),availableAmount,createUser,returnSmsOrder.getRemark());
            //??????????????????
            LambdaUpdateWrapper<Agent> updateAgentWrapper = new LambdaUpdateWrapper<>();
            updateAgentWrapper.setSql("available_amount=available_amount-"+agentReturnAmount);
            updateAgentWrapper.setSql("send_sms_total=send_sms_total-"+returnSmsOrder.getSmsNum());
            updateAgentWrapper.setSql("data_md5=UPPER(MD5(CONCAT('"+ AgentServiceImpl.Md5Key+"',agent_account,amount,Available_Amount,Cash_Amount,Send_Sms_Total,quota_amount)))");
            updateAgentWrapper.eq(Agent::getId, agentId);
            agentService.update(updateAgentWrapper);
        }


        Integer upAgentId = returnSmsOrder.getUpAgentId();
        if(upAgentId == null) {
            return;
        }
        Integer upAgentReturnAmount = returnSmsOrder.getUpAgentReturnAmount();
        if(upAgentReturnAmount < 0) {
            String errMsg = String.format("???????????????%d????????????????????????%f?????????",returnSmsOrder.getAgentId(),upAgentReturnAmount/100);
            throw new ServiceException(errMsg);
        }

        Agent upAgent = agentService.queryOrgByIdForUpdate(upAgentId);
        if(upAgent == null) {
            String errMsg = String.format("???????????????%d????????????",upAgentId);
            throw new ServiceException(errMsg);
        }
        String upAgentMd5Data = agentService.getDataMd5(upAgent);
        if (!upAgent.getDataMd5().equals(upAgentMd5Data)){
            String errMsg = String.format("?????????%d???????????????????????????",upAgentId);
            throw new ServiceException(errMsg);
        }

        if(upAgentReturnAmount > 0) {
            //????????????????????????????????????
            int availableAmount = upAgent.getAvailableAmount().intValue() - upAgentReturnAmount;
            savaAgentRechargeOrder(upAgentReturnAmount,String.valueOf(upAgentId),availableAmount,createUser,returnSmsOrder.getRemark());
            //????????????????????????
            LambdaUpdateWrapper<Agent> updateAgentWrapper = new LambdaUpdateWrapper<>();
            updateAgentWrapper.setSql("available_amount=available_amount-"+upAgentReturnAmount);
            updateAgentWrapper.setSql("send_sms_total=send_sms_total-"+returnSmsOrder.getSmsNum());
            updateAgentWrapper.setSql("data_md5=UPPER(MD5(CONCAT('"+AgentServiceImpl.Md5Key+"',agent_account,amount,Available_Amount,Cash_Amount,Send_Sms_Total,quota_amount)))");
            updateAgentWrapper.eq(Agent::getId, upAgentId);
            agentService.update(updateAgentWrapper);
        }
    }

    /**
     * ????????????????????????????????????????????????
     * @param rechargeAmount
     * @param agentId
     * @param availableAmount
     * @param createUser
     */
    public void savaAgentRechargeOrder(Integer rechargeAmount,String agentId,Integer availableAmount,String createUser,String remark){
        AgentRechargeOrder agentRechargeOrder = new AgentRechargeOrder();
        agentRechargeOrder.setAgentId(agentId);
        agentRechargeOrder.setCeateUser(createUser);
        agentRechargeOrder.setRechargeAmount(rechargeAmount);
        agentRechargeOrder.setNetwayCode("SMSRETURN");
        agentRechargeOrder.setCreateTime(new Date());
        agentRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        agentRechargeOrder.setPayChannelId("");
        agentRechargeOrder.setPayMerchNo("");
        agentRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","????????????").getCode());
        agentRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setAvailableAmount(availableAmount);
        agentRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","??????").getCode());
        agentRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","??????").getCode());
        agentRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        agentRechargeOrder.setRemark(remark);
        this.agentRechargeOrderService.save(agentRechargeOrder);
    }

    /**
     * ????????????????????????????????????????????????
     * @param rechargeAmount
     * @param orgCode
     * @param availableAmount
     * @param createUser
     */
    public void saveOrgReChargeOrder(Integer rechargeAmount, String orgCode, Integer availableAmount, String createUser,String remark){
        OrganizationRechargeOrder organizationRechargeOrder = new OrganizationRechargeOrder();
        organizationRechargeOrder.setRechargeAmount(rechargeAmount);
        organizationRechargeOrder.setNetwayCode("SMSRETURN");
        organizationRechargeOrder.setOrganizationCode(orgCode);
        organizationRechargeOrder.setCeateUser(createUser);
        organizationRechargeOrder.setCreateTime(new Date());
        organizationRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
        organizationRechargeOrder.setPayChannelId("");
        organizationRechargeOrder.setPayMerchNo("");
        organizationRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","????????????").getCode());
        organizationRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setAvailableAmount(availableAmount);
        organizationRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","??????").getCode());
        organizationRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","??????").getCode());
        organizationRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        organizationRechargeOrder.setRemark(remark);
        this.organizationRechargeOrderService.save(organizationRechargeOrder);
    }
    /**
     * ??????????????????
     * @param returnSmsOrder
     * @throws ServiceException
     */
    public void updateOrgAmout(ReturnSmsOrder returnSmsOrder,String createUser) throws ServiceException{
        Integer orgReturnAmount = returnSmsOrder.getOrgReturnAmount();
        if(orgReturnAmount <0) {
            String errMsg = String.format("?????????%s??????????????????%f?????????",returnSmsOrder.getOrgCode(),orgReturnAmount/100.0);
            throw new ServiceException(errMsg);
        }
        Organization organization = organizationService.queryOrgByCodeForUpdate(returnSmsOrder.getOrgCode());
        if (organization == null){
            String errMsg = String.format("?????????%s????????????",returnSmsOrder.getOrgCode());
            throw new ServiceException(errMsg);
        }
        String md5Data = organizationService.getDataMd5(organization);
        if (!organization.getDataMd5().equals(md5Data)){
            String errMsg = String.format("?????????%s???????????????????????????",returnSmsOrder.getOrgCode());
            throw new ServiceException(errMsg);
        }
        if (returnSmsOrder.getOrgReturnAmount() > 0){
            //??????????????????????????????
            int rechargeAmount = returnSmsOrder.getOrgReturnAmount();
            int availableAmount = organization.getAvailableAmount().intValue() + rechargeAmount;
            saveOrgReChargeOrder(rechargeAmount,returnSmsOrder.getOrgCode(),availableAmount,createUser,returnSmsOrder.getRemark());

            //??????
            LambdaUpdateWrapper<Organization> updateOrgWrapper = new LambdaUpdateWrapper<>();
            updateOrgWrapper.setSql("cash_amount=cash_amount-"+orgReturnAmount);
            updateOrgWrapper.setSql("available_amount=available_amount+"+orgReturnAmount);
            updateOrgWrapper.setSql("send_sms_total=send_sms_total-"+returnSmsOrder.getSmsNum());
            updateOrgWrapper.setSql("data_md5=UPPER(MD5(CONCAT('"+ OrganizationServiceImpl.Md5Key+"',Organization_Code,amount,Available_Amount,Cash_Amount,Send_Sms_Total,Md5_Key,Sms_Sign,Bind_IP)))");
            updateOrgWrapper.eq(Organization::getId, organization.getId());
            organizationService.update(updateOrgWrapper);
        }
    }

    @Override
    @Transactional
    public void updateReturnSmsOrder(ReturnSmsOrder returnSmsOrder) {
        this.saveOrUpdate(returnSmsOrder);
    }

    @Override
    @Transactional
    public void deleteReturnSmsOrder(ReturnSmsOrderQuery returnSmsOrder) {
        LambdaQueryWrapper<ReturnSmsOrder> wrapper = new LambdaQueryWrapper<>();
	    // TODO ??????????????????
        if (StringUtils.isNotBlank(returnSmsOrder.getOrderNo())){//?????????
            wrapper.eq(ReturnSmsOrder::getOrderNo,returnSmsOrder.getOrderNo());
        }
        if (StringUtils.isNotBlank(returnSmsOrder.getOrgCode())){
            wrapper.eq(ReturnSmsOrder::getOrgCode,returnSmsOrder.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnSmsOrder.getSendCode())){
            wrapper.eq(ReturnSmsOrder::getSendCode,returnSmsOrder.getSendCode());
        }
        if (returnSmsOrder.getAgentId() != null){
            wrapper.eq(ReturnSmsOrder::getAgentId,returnSmsOrder.getAgentId());
        }
        if (returnSmsOrder.getCreateStartTime() != null){
            wrapper.ge(ReturnSmsOrder::getCreateTime,returnSmsOrder.getCreateStartTime());
        }
        if (returnSmsOrder.getCreateEndTime() != null){
            wrapper.le(ReturnSmsOrder::getCreateTime,returnSmsOrder.getCreateEndTime());
        }
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteReturnSmsOrders(String[] returnSmsOrderIds) {
        List<String> list = Arrays.asList(returnSmsOrderIds);
        this.removeByIds(list);
    }
}
