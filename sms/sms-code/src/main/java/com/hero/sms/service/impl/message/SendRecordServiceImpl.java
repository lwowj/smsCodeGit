package com.hero.sms.service.impl.message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.common.ExportRecord;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.NotifyMsgStateModel;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoPlan;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.entity.message.exportModel.SendRecordExcel;
import com.hero.sms.entity.message.exportModel.SendRecordExtExcel;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.channel.SmsChannelProtocolTypeEnums;
import com.hero.sms.enums.channel.SmsChannelSubmitWayEnums;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.common.ExportTypeEnums;
import com.hero.sms.enums.common.UserTypeEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.mapper.message.SendRecordMapper;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.common.IExportRecordService;
import com.hero.sms.service.impl.channel.push.BaseSmsPushService;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendRecordCheckinfoPlanService;
import com.hero.sms.service.message.ISendRecordCheckinfoService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.utils.RandomUtil;
import com.wuwenze.poi.ExcelKit;

/**
 * 发送记录 Service实现
 *
 * @author Administrator
 * @date 2020-03-07 23:20:22
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendRecordServiceImpl extends ServiceImpl<SendRecordMapper, SendRecord> implements ISendRecordService {

    @Autowired
    private SendRecordMapper sendRecordMapper;
    @Autowired
    private ISmsChannelService smsChannelService;
    @Autowired
    private IOrganizationService organizationService;

    @Lazy
    @Autowired
    private IReturnRecordService returnRecordService;
    
	/**
	 * 这里实例本身类的作用是防止事务失效
	 * 代码中的一些方法，若本类直接调用本类方法会出现事务失效
	 */
    @Autowired
    @Lazy
    private ISendRecordService sendRecordService;

    @Autowired
    private IExportRecordService exportRecordService;
    
    @Autowired
    private ISendRecordCheckinfoService sendRecordCheckinfoService;
    
    @Autowired
    private ISendRecordCheckinfoPlanService sendRecordCheckinfoPlanService;

    @Override
    public IPage<SendRecord> findSendRecords(QueryRequest request, SendRecordQuery sendRecord) {
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//商户编码
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//手机号码
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        /**
         * 2021-04-17
         * @begin
         */
        if (sendRecord.getSubmitStartTime() != null){//起始时间 for 提交时间
            queryWrapper.ge(SendRecord::getSubmitTime,sendRecord.getSubmitStartTime());
        }
        if (sendRecord.getSubmitEndTime() != null){//结束时间 for 提交时间
            queryWrapper.le(SendRecord::getSubmitTime,sendRecord.getSubmitEndTime());
        }
        /**
         * @end
         */
        if (sendRecord.getChannelId() != null){//通道
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//状态
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getResMsgid())){//通道标识
            queryWrapper.eq(SendRecord::getResMsgid,sendRecord.getResMsgid());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (sendRecord.getAgentId() != null){
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//短信内容
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//号码归属地
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getSmsCount() != null){//有效短信
            queryWrapper.eq(SendRecord:: getSmsCount,sendRecord.getSmsCount());
        }
        if (StringUtils.isNotBlank(sendRecord.getStateDesc())){//备注内容
            queryWrapper.like(SendRecord::getStateDesc,sendRecord.getStateDesc());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(SendRecord::getSmsNumberArea, sendRecord.getSmsNumberArea());
		}
		if (sendRecord.getGroupId() != null){
            queryWrapper.inSql(BaseSend::getOrgCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",sendRecord.getGroupId()));
        }
        /**
         * @begin 2020-11-17
         * 新增查询条件
         */
        //通道id是否为空
	   	if (sendRecord.getIsChannelIdFlag() != null) 
	   	{
			if (sendRecord.getIsChannelIdFlag())
			{
				queryWrapper.isNotNull(SendRecord:: getChannelId);
			}
			else
			{
				queryWrapper.isNull(SendRecord:: getChannelId);
			}
		}
	   	//消费金额是否为空
	   	if (sendRecord.getIsConsumeAmountFlag() != null) 
	   	{
			if (sendRecord.getIsConsumeAmountFlag())
			{
				queryWrapper.isNotNull(SendRecord:: getConsumeAmount);
			}
			else
			{
				queryWrapper.isNull(SendRecord:: getConsumeAmount);
			}
		}
	   	//消费金额不等于某值
	   	if (sendRecord.getNotIsConsumeAmount() != null)
	   	{
	   		queryWrapper.ne(SendRecord::getConsumeAmount, sendRecord.getNotIsConsumeAmount());
        }
	   	//状态数组
	   	if (sendRecord.getStateArray() != null)
	   	{
	   		if(sendRecord.getStateArray().length>0)
	   		{
	   			queryWrapper.in(SendRecord::getState,sendRecord.getStateArray());
	   		}
        }
	   	/**
	   	 * @begin
	   	 * 2021-07-02
	   	 * 通道resid是否为空
	   	 */
        //通道resid是否为空
	   	if (sendRecord.getResMsgidIsNullFlag() != null) 
	   	{
			if (sendRecord.getResMsgidIsNullFlag())
			{
				queryWrapper.isNull(SendRecord::getResMsgid);
			}
			else
			{
				queryWrapper.isNotNull(SendRecord:: getResMsgid);
			}
		}
	   	/**
	   	 * @end
	   	 */
	   	//创建时间的排序方式
	   	if(sendRecord.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(sendRecord.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(SendRecord::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(SendRecord::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(SendRecord::getCreateTime);
	   	}
	   	/**
	   	 * @end
	   	 */
        
        // TODO 设置查询条件
        Page<SendRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SendRecord> findSendRecords(SendRecordQuery sendRecord) {
	    LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
	    if (StringUtils.isNotBlank(sendRecord.getBeforeApplyString())){
	        queryWrapper.apply(sendRecord.getBeforeApplyString());
        }
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//商户编码
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//手机号码
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        /**
         * 2021-04-17
         * @begin
         */
        if (sendRecord.getSubmitStartTime() != null){//起始时间 for 提交时间
            queryWrapper.ge(SendRecord::getSubmitTime,sendRecord.getSubmitStartTime());
        }
        if (sendRecord.getSubmitEndTime() != null){//结束时间 for 提交时间
            queryWrapper.le(SendRecord::getSubmitTime,sendRecord.getSubmitEndTime());
        }
        /**
         * @end
         */
        if (sendRecord.getChannelId() != null){//通道
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getAgentId() != null){//代理id
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (sendRecord.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//状态
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//短信内容
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecord.getResMsgid())){//通道标识
            queryWrapper.eq(SendRecord::getResMsgid,sendRecord.getResMsgid());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//号码归属地
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getSmsCount() != null){//有效短信
            queryWrapper.eq(SendRecord:: getSmsCount,sendRecord.getSmsCount());
        }
        if (StringUtils.isNotBlank(sendRecord.getStateDesc())){//备注内容
            queryWrapper.like(SendRecord::getStateDesc,sendRecord.getStateDesc());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(SendRecord::getSmsNumberArea, sendRecord.getSmsNumberArea());
		}
		if (sendRecord.getGroupId() != null){
            queryWrapper.inSql(BaseSend::getOrgCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",sendRecord.getGroupId()));
        }
        /**
         * @begin 2020-11-17
         * 新增查询条件
         */
        //通道id是否为空
	   	if (sendRecord.getIsChannelIdFlag() != null) 
	   	{
			if (sendRecord.getIsChannelIdFlag())
			{
				queryWrapper.isNotNull(SendRecord:: getChannelId);
			}
			else
			{
				queryWrapper.isNull(SendRecord:: getChannelId);
			}
		}
	   	//消费金额是否为空
	   	if (sendRecord.getIsConsumeAmountFlag() != null) 
	   	{
			if (sendRecord.getIsConsumeAmountFlag())
			{
				queryWrapper.isNotNull(SendRecord:: getConsumeAmount);
			}
			else
			{
				queryWrapper.isNull(SendRecord:: getConsumeAmount);
			}
		}
	   	//消费金额不等于某值
	   	if (sendRecord.getNotIsConsumeAmount() != null)
	   	{
	   		queryWrapper.ne(SendRecord::getConsumeAmount, sendRecord.getNotIsConsumeAmount());
        }
	   	//状态数组
	   	if (sendRecord.getStateArray() != null)
	   	{
	   		if(sendRecord.getStateArray().length>0)
	   		{
	   			queryWrapper.in(SendRecord::getState,sendRecord.getStateArray());
	   		}
        }
	   	/**
	   	 * @begin
	   	 * 2021-07-02
	   	 * 通道resid是否为空
	   	 */
        //通道resid是否为空
	   	if (sendRecord.getResMsgidIsNullFlag() != null) 
	   	{
			if (sendRecord.getResMsgidIsNullFlag())
			{
				queryWrapper.isNull(SendRecord::getResMsgid);
			}
			else
			{
				queryWrapper.isNotNull(SendRecord:: getResMsgid);
			}
		}
	   	/**
	   	 * @end
	   	 */
		//创建时间的排序方式
	   	if(sendRecord.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(sendRecord.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(SendRecord::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(SendRecord::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(SendRecord::getCreateTime);
	   	}
	   	/**
	   	 * @end
	   	 */
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSendRecord(SendRecord sendRecord) {
        this.save(sendRecord);
    }

    @Override
    @Transactional
    public void updateSendRecord(SendRecord sendRecord) {
        this.saveOrUpdate(sendRecord);
    }

    @Override
    @Transactional
    public void deleteSendRecord(SendRecordQuery sendRecord) {
        LambdaQueryWrapper<SendRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//商户编码
            wrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            wrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//手机号码
            wrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            wrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            wrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null){//通道
            wrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getAgentId() != null){//代理id
            wrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (sendRecord.getUpAgentId() != null){//上级代理id
            wrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//状态
            wrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            wrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSendRecords(String[] sendRecordIds) {
        List<String> list = Arrays.asList(sendRecordIds);
        this.removeByIds(list);
    }

	@Override
	public void pushHttpMsg(Long sendRecordId) {
		SendRecord sendRecord = getById(sendRecordId);
		if(sendRecord == null) return;
		if(sendRecord.getChannelId() == null) return;
		SmsChannelExt smsChannelExt = smsChannelService.findContainPropertyById(sendRecord.getChannelId());

        if (smsChannelExt == null){
        	log.error(String.format("发送记录【%s】走的通道【%s】不存在", sendRecordId,sendRecord.getChannelId()));
            return;
        }
        String protocolType = smsChannelExt.getProtocolType();
        if(!SmsChannelProtocolTypeEnums.Http.getCode().equals(protocolType)) {
        	log.error(String.format("发送记录【%s】走的通道【%s】协议错误", sendRecordId,smsChannelExt.getId()));
        	return;
        }
        
        /**
		 * @begin 2021-10-23
		 * 发送时校验通道状态是否为开启
		 */
		String checkChannelStateSwitch = "OFF";
 		Code checkChannelStateSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkChannelStateSwitch");
 	    if(checkChannelStateSwitchCode!=null&&!"".equals(checkChannelStateSwitchCode.getName()))
 	    {
 	    	checkChannelStateSwitch = checkChannelStateSwitchCode.getName();
 	    }
 	    if("ON".equals(checkChannelStateSwitch))
 	    {
 	    	if(smsChannelExt.getState() != ChannelStateEnums.START.getCode())
 	    	{
 	    		log.error(String.format("发送记录【%s】走的通道【%s】不是开启状态,无法正常提交", sendRecordId,sendRecord.getChannelId()));
 	            return;
 	    	}
 	    }
 	   /**
 	    * @end
 	    */
 	    
        /**
		 * @begin 2020-12-21
		 * 将同批次同号码入库中间校验表，若入库失败则表示该批次该号码已提交发送，不可重复发送
		 * 若需再次提交，则需修改中间校验状态，有且只能修改一次
		 * 
		 */
        String sendRecordCheckinfoSwitch = "ON";
 		Code sendRecordCheckinfoSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckinfoSwitch");
 	    if(sendRecordCheckinfoSwitchCode!=null&&!"".equals(sendRecordCheckinfoSwitchCode.getName()))
 	    {
 	    	sendRecordCheckinfoSwitch = sendRecordCheckinfoSwitchCode.getName();
 	    }
 	    if(!"OFF".equals(sendRecordCheckinfoSwitch))
 	    {

    		String sendRecordCheckInfoWay = "0";
    		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
    	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
    	    {
    	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
    	    }
 	    	
 	    	try 
 	    	{
 	    		 //1、通过记录id校验； 0或其他 通过批次号与号码校验
 	    	    if("1".equals(sendRecordCheckInfoWay))
 	    	    {
 	    	    	SendRecordCheckinfoPlan sendRecordCheckinfoPlan = new SendRecordCheckinfoPlan();
 	    	    	sendRecordCheckinfoPlan.setSendRecordId(sendRecord.getId());
 	    	    	sendRecordCheckinfoPlan.setState(1);
 	    	    	sendRecordCheckinfoPlanService.createSendRecordCheckinfoPlan(sendRecordCheckinfoPlan);
 	    	    }
 	    	    else
 	    	    {
 	 				SendRecordCheckinfo sendRecordCheckinfo = new SendRecordCheckinfo();
 	 				sendRecordCheckinfo.setSendCode(sendRecord.getSendCode());
 	 				sendRecordCheckinfo.setChannelId(sendRecord.getChannelId());
 	 				sendRecordCheckinfo.setSmsNumber(sendRecord.getSmsNumber());
 	 				sendRecordCheckinfo.setState(1);
 	 				sendRecordCheckinfoService.createSendRecordCheckinfo(sendRecordCheckinfo);
 	    	    }    
 			} catch (Exception e) {
 				// TODO: handle exception
 				log.error(String.format("HTTP发送记录【%s】该批次【%s】该号码【%s】已提交过发送", sendRecordId,sendRecord.getSendCode(),sendRecord.getSmsNumber()));
 				try {
 					SendRecord entity = new SendRecord();
 					entity.setId(sendRecord.getId());
 					entity.setStateDesc("本批次该号码已提交过发送");
 					sendRecordService.updateById(entity);
 				} catch (Exception e2) {}
 				return;
 			}
 	    }
		/**
		 * @end
		 */
        BaseSmsPushService pushService ;
        try {
            pushService = (BaseSmsPushService) Class.forName(smsChannelExt.getImplFullClass()).newInstance();
            pushService.init(smsChannelExt);
            boolean result = pushService.push(sendRecord);
            if(result) {
            	sendRecord.setState(SendRecordStateEnums.ReqSuccess.getCode());
            }else {
            	sendRecord.setState(SendRecordStateEnums.ReqFail.getCode());
            }
        } catch (Exception e) {
        	log.error(String.format("消息推送失败【%d】",sendRecord.getId()),e);
        	sendRecord.setState(SendRecordStateEnums.ReqFail.getCode());
            sendRecord.setStateDesc("通道请求失败");
        }

        LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SendRecord::getState, sendRecord.getState());
        updateWrapper.set(SendRecord::getStateDesc, sendRecord.getStateDesc());
        updateWrapper.set(SendRecord::getResMsgid, sendRecord.getResMsgid());
        //2020-12-01 添加记录请求上游的时间
        updateWrapper.set(SendRecord::getSubmitTime, new Date());
        updateWrapper.eq(SendRecord::getId, sendRecord.getId());
        update(updateWrapper);
	}

	@Override
	public void pushMsg(String protocolType,Long sendRecordId) 
	{
		String pushMsgBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
		String showRunTimeLogSwitch = "OFF";
 		Code showRunTimeLogSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogSwitch");
 	    if(showRunTimeLogSwitchCode!=null&&!"".equals(showRunTimeLogSwitchCode.getName()))
 	    {
 	    	showRunTimeLogSwitch = showRunTimeLogSwitchCode.getName();
 	    }
 	    
		String sendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
		//根据ID查询发送记录
		SendRecord sendRecord = getById(sendRecordId);
		if(sendRecord == null) return;
		if(sendRecord.getChannelId() == null) return;
		//判断发送记录的状态是否是“等待提交”“提交失败”状态
		if(sendRecord.getState().intValue() != SendRecordStateEnums.WaitReq.getCode() &&  sendRecord.getState().intValue() != SendRecordStateEnums.ReqFail.getCode()) return;
		
		if("ON".equals(showRunTimeLogSwitch)) 
 	    {
			String sendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			long sendRecordRuntime = DateUtil.getTime(sendRecordBeginTime, sendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
			log.error(String.format("【1、提交发送查询sendRecord耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),sendRecordBeginTime,sendRecordEndTime,String.valueOf(sendRecordRuntime)));
 	    }
		
	    String findChannelBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
	    
	    /**
		 * @begin 2022-05-02
		 * 查询记录对应通道方式调整
		 */
		SmsChannel smsChannel = null;
		String querySmppChannelWay = "data";
 		Code querySmppChannelWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","querySmppChannelWay");
 	    if(querySmppChannelWayCode!=null&&!"".equals(querySmppChannelWayCode.getName()))
 	    {
 	    	querySmppChannelWay = querySmppChannelWayCode.getName();
 	    }
 	    boolean querySmsChannelFlag = true;
 	    if("cache".equals(querySmppChannelWay))
 	    {
 	    	List<SmsChannel> getSmsChannelList = DatabaseCache.getSmsChannelList();
 	    	if(getSmsChannelList != null && getSmsChannelList.size()>0)
 	    	{
 	    		smsChannel = DatabaseCache.getSmsChannelById(sendRecord.getChannelId());
 	    		querySmsChannelFlag = false;
 	    	}
 	    }
 	    
 	    if(querySmsChannelFlag)
 	    {
 	    	smsChannel = smsChannelService.getById(sendRecord.getChannelId());
 	    }
		/**
		 * @end
		 */
 	    
		if (smsChannel == null){
        	log.error(String.format("发送记录【%s】走的通道【%s】不存在", sendRecordId,sendRecord.getChannelId()));
            return;
        }
		/**
		 * @begin 2021-10-23
		 * 发送时校验通道状态是否为开启
		 */
		String checkChannelStateSwitch = "OFF";
 		Code checkChannelStateSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkChannelStateSwitch");
 	    if(checkChannelStateSwitchCode!=null&&!"".equals(checkChannelStateSwitchCode.getName()))
 	    {
 	    	checkChannelStateSwitch = checkChannelStateSwitchCode.getName();
 	    }
 	    if("ON".equals(checkChannelStateSwitch))
 	    {
 	    	if(smsChannel.getState() != ChannelStateEnums.START.getCode())
 	    	{
 	    		log.error(String.format("发送记录【%s】走的通道【%s】不是开启状态,无法正常提交", sendRecordId,sendRecord.getChannelId()));
 	            return;
 	    	}
 	    }
 	   /**
 	    * @end
 	    */
 	    
		if("ON".equals(showRunTimeLogSwitch)) 
 	    {
			String findChannelEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			long findChannelRuntime = DateUtil.getTime(findChannelBeginTime, findChannelEndTime,DateUtil.Y_M_D_H_M_S_S_2);
			log.error(String.format("【2、提交发送查询smsChannelExt耗时】【查询方式：%s】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",querySmppChannelWay,sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),findChannelBeginTime,findChannelEndTime,String.valueOf(findChannelRuntime)));
 	    }
		
		/**
		 * @begin 2020-12-21
		 * 将同批次同号码入库中间校验表，若入库失败则表示该批次该号码已提交发送，不可重复发送
		 * 若需再次提交，则需修改中间校验状态，有且只能修改一次
		 * 
		 */
		String sendRecordCheckinfoSwitch = "ON";
 		Code sendRecordCheckinfoSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckinfoSwitch");
 	    if(sendRecordCheckinfoSwitchCode!=null&&!"".equals(sendRecordCheckinfoSwitchCode.getName()))
 	    {
 	    	sendRecordCheckinfoSwitch = sendRecordCheckinfoSwitchCode.getName();
 	    }
 	    if(!"OFF".equals(sendRecordCheckinfoSwitch))
 	    {
 	    	String sendRecordCheckInfoWay = "0";
    		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
    	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
    	    {
    	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
    	    }	
    	    
 	    	try 
 	    	{
 	    		String checkSendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 	    		 //1、通过记录id校验； 0或其他 通过批次号与号码校验
 	    	    if("1".equals(sendRecordCheckInfoWay))
 	    	    {
 	    	    	SendRecordCheckinfoPlan sendRecordCheckinfoPlan = new SendRecordCheckinfoPlan();
 	    	    	sendRecordCheckinfoPlan.setSendRecordId(sendRecord.getId());
 	    	    	sendRecordCheckinfoPlan.setState(1);
 	    	    	sendRecordCheckinfoPlanService.createSendRecordCheckinfoPlan(sendRecordCheckinfoPlan);
 	    	    }
 	    	    else
 	    	    {
 	 				SendRecordCheckinfo sendRecordCheckinfo = new SendRecordCheckinfo();
 	 				sendRecordCheckinfo.setSendCode(sendRecord.getSendCode());
 	 				sendRecordCheckinfo.setChannelId(sendRecord.getChannelId());
 	 				sendRecordCheckinfo.setSmsNumber(sendRecord.getSmsNumber());
 	 				sendRecordCheckinfo.setState(1);
 	 				sendRecordCheckinfoService.createSendRecordCheckinfo(sendRecordCheckinfo);
 	    	    } 
 				
 				if("ON".equals(showRunTimeLogSwitch)) 
 		 	    {
 					String checkSendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 					long checkSendRecordRuntime = DateUtil.getTime(checkSendRecordBeginTime, checkSendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
 					log.error(String.format("【3、提交发送校验唯一发送耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),checkSendRecordBeginTime,checkSendRecordEndTime,String.valueOf(checkSendRecordRuntime)));
 		 	    }
 			} catch (Exception e) {
 				// TODO: handle exception
 				log.error(String.format("SMPP发送记录【%s】该批次【%s】该号码【%s】已提交过发送", sendRecordId,sendRecord.getSendCode(),sendRecord.getSmsNumber()));
 				String updateSendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 				try {
 					SendRecord entity = new SendRecord();
 					entity.setId(sendRecord.getId());
 					entity.setStateDesc("本批次该号码已提交过发送");
 					sendRecordService.updateById(entity);
 				} catch (Exception e2) {}
 				
 				if("ON".equals(showRunTimeLogSwitch)) 
 		 	    {
	 				String updateSendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
	 				long updateSendRecordRuntime = DateUtil.getTime(updateSendRecordBeginTime, updateSendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
	 			    log.error(String.format("【4、提交发送已发送修改状态耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),updateSendRecordBeginTime,updateSendRecordEndTime,String.valueOf(updateSendRecordRuntime)));
 		 	    }
 				
 				return;
 			}
 	    }
		/**
		 * @end
		 */
        BaseSmsPushService pushService = new BaseSmsPushService();
//        pushService.init(smsChannelExt);
        if(protocolType.equals(SmsChannelProtocolTypeEnums.Smpp.getCode())) {
        	//3-1-2-2-1 根据上游通道进行推送
        	String pushBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
        	/**
        	 * @begin 2022-05-09
        	 * 根据配置是否开启通道自身提交方式
        	 * 提交方式开关  ON:根据通道自身设置 ； OFF或其他值 为 全部同步提交
        	 * 如果短信内容字符大于70，则默认选择同步提交方式
        	 */
        	//sendSmsWayAsynFlag是否异步发送标记
        	boolean sendSmsWayAsynFlag = false; 
        	
        	String sendSmsWaySwitch = "OFF";
    		Code sendSmsWaySwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsWaySwitch");
    	    if(sendSmsWaySwitchCode!=null&&!"".equals(sendSmsWaySwitchCode.getName()))
    	    {
    	    	sendSmsWaySwitch = sendSmsWaySwitchCode.getName();
    	    }
    	    //当配置开关开启，则判断通道自身配置，是否为异步提交
    	    //submitWay  0同步 1异步
    	    if("ON".equals(sendSmsWaySwitch))
    	    {
    	    	int submitWay = 0;
    	    	try {
    	    		submitWay = smsChannel.getSubmitWay();
				} catch (Exception e) {}
    	    	if(submitWay == SmsChannelSubmitWayEnums.ASYN.getCode())
    	    	{
    	    		//如果短信内容字符大于70，则默认选择同步提交方式
            	    int smsWords = sendRecord.getSmsWords();
            	    if(smsWords>70)
            	    {
            	    	sendSmsWayAsynFlag = false;
            	    }
            	    else
            	    {
            	    	sendSmsWayAsynFlag = true;
            	    }
    	    	}
    	    }
    	    //sendSmsWayAsynFlag 根据筛选的标记进行方法调用；true 异步，false 同步
    	    String sendSmsWayString = "";
    	    if(sendSmsWayAsynFlag)
    	    {
    	    	sendSmsWayString = "异步提交";
    	    	pushService.smppPushAsyn(sendRecord);
    	    }
    	    else
    	    {
    	    	sendSmsWayString = "同步提交";
    	    	pushService.smppPush(sendRecord);
    	    }
    	    /**
    	     * @end
    	     */
        	if("ON".equals(showRunTimeLogSwitch)) 
        	{
 				String pushEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 				long pushRuntime = DateUtil.getTime(pushBeginTime, pushEndTime,DateUtil.Y_M_D_H_M_S_S_2);
 			    log.error(String.format("【5、提交发送调用接口耗时】【%s】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendSmsWayString,sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),pushBeginTime,pushEndTime,String.valueOf(pushRuntime)));
		 	}
        }
        else
        {
        	log.error(String.format("发送记录【%s】走的通道【%s】协议错误", sendRecordId,smsChannel.getId()));
        }
        Code showRunTimeLogMainSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogMainSwitch");
        String showRunTimeLogMainSwitch = "OFF";
 	    if(showRunTimeLogMainSwitchCode!=null&&!"".equals(showRunTimeLogMainSwitchCode.getName()))
 	    {
 	    	showRunTimeLogMainSwitch = showRunTimeLogMainSwitchCode.getName();
 	    }
        if("ON".equals(showRunTimeLogMainSwitch)) 
 	    {
			String pushMsgEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			long pushMsgRuntime = DateUtil.getTime(pushMsgBeginTime, pushMsgEndTime,DateUtil.Y_M_D_H_M_S_S_2);
			log.error(String.format("【SMPP发送总耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),pushMsgBeginTime,pushMsgEndTime,String.valueOf(pushMsgRuntime)));
 	    }
	}

	@Override
	public SendRecord getByAreaAndNumberAndMsgId(String area,String smsNumber, String resMsgid) {
		LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SendRecord::getSmsNumberArea, area);
		queryWrapper.eq(SendRecord::getSmsNumber, smsNumber);
		queryWrapper.eq(SendRecord::getResMsgid, resMsgid);
		return getOne(queryWrapper, true);
	}

	/**
	 * @begin 2021-10-22
	 * 根据通道id、上游通道标识 查询发送记录
	 */
	@Override
	public SendRecord getByChannelIdAndMsgId(int channelId, String resMsgid) {
		LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SendRecord::getChannelId, channelId);
		queryWrapper.eq(SendRecord::getResMsgid, resMsgid);
		return getOne(queryWrapper, true);
	}
    /**
     * 按条件批量回执
     * @param sendRecord
     * @return
     */
	@Override
    public int batchNotifyMsgState(SendRecordQuery sendRecord){
        sendRecord.setBeforeApplyString("`state` & 49 = `state`");
        List<SendRecord> list = this.findSendRecords(sendRecord);
        return batchNotifyMsgState(list);
    }

    /**
     * 按勾选批量回执
     * @param sendRecordIds
     * @return
     */
    @Override
    public int batchNotifyMsgState(String sendRecordIds){
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("`state` & 49 = `state`")
                .in(SendRecord::getId,ids);
        List<SendRecord> list = list(queryWrapper);
        return batchNotifyMsgState(list);
    }

    //手动批量回执通知
    public int batchNotifyMsgState(List<SendRecord> list){
        AtomicInteger count = new AtomicInteger();
        //按商户分组
        Map<String,List<SendRecord>> orgMap = list.stream()
                .collect(Collectors.groupingBy(SendRecord::getOrgCode));
        //补发通知
        Set<Map.Entry<String, List<SendRecord>>> entrysets = orgMap.entrySet();
        entrysets.stream().forEach(stringListEntry -> {
            String orgCode = stringListEntry.getKey();
            Organization org = organizationService.getOrganizationByCode(orgCode);
            if (org == null){
                log.error(String.format("商户【%s】不存在，补发通知失败！",orgCode));
                return;
            }
            /*if (StringUtils.isBlank(org.getNotifyUrl())){
                log.error(String.format("商户【%s】通知地址没有配置，补发通知失败！",orgCode));
                return;
            }*/

            stringListEntry.getValue().stream().forEach(sendRecord -> {
                NotifyMsgStateModel notifyMsgStateModel = new NotifyMsgStateModel();
                notifyMsgStateModel.setSourceNumber(sendRecord.getSourceNumber());
                notifyMsgStateModel.setMobile(sendRecord.getSmsNumber());
                notifyMsgStateModel.setMobileArea(sendRecord.getSmsNumberArea());
                notifyMsgStateModel.setOrgCode(sendRecord.getOrgCode());
                notifyMsgStateModel.setSendCode(sendRecord.getSendCode());
                notifyMsgStateModel.setSubType(sendRecord.getSubType());
                notifyMsgStateModel.setState(String.valueOf(sendRecord.getState()));
                notifyMsgStateModel.setMsg(sendRecord.getStateDesc());
                if(sendRecord.getReceiptTime() != null) {
                	notifyMsgStateModel.setReceiptTime(sendRecord.getReceiptTime());
                }else {
                	notifyMsgStateModel.setReceiptTime(sendRecord.getCreateTime());
                }
                try {
                    organizationService.notifyMsgState(org,notifyMsgStateModel);
                    count.getAndIncrement();
                } catch (Exception e) {
                    log.error(String.format("发送记录【id:%s】通知状态失败",sendRecord.getId()),e);
                }
            });
        });
        return count.intValue();
    }

    @Override
    public int batchUpdateMsgReturnState(String sendRecordIds, Boolean success) {

        if (StringUtils.isBlank(sendRecordIds) || success == null ){
            log.warn("【批量修改回执状态】参数错误或无效，更新失败");
            return 0;
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        //只有提交成功状态的消息可以强制修改回执状态
        queryWrapper.apply("`state` = 4")
                .in(SendRecord::getId,ids);
        List<SendRecord> list = list(queryWrapper);

        return sendRecordService.batchUpdateMsgReturnState(list,success);
    }

    @Override
    public String batchUpdateMsgReturnState(SendRecordQuery sendRecordQuery, BigDecimal successRate) {
        String resultMsgFormat = "成功修改【%s】条发送记录的回执状态，【%s】条成功，【%s】条失败";
        if (sendRecordQuery == null || successRate == null ){
            log.warn("【批量修改回执状态】参数错误或无效，更新失败");
            return String.format(resultMsgFormat,0,0,0);
        }
        sendRecordQuery.setBeforeApplyString("`state` = 4");
        List<SendRecord> list = this.findSendRecords(sendRecordQuery);
        int total = list.size();
        //回执成功的条数
        int successNum  = successRate.divide(new BigDecimal(100)).multiply(new BigDecimal(total)).setScale(0,BigDecimal.ROUND_UP).intValue();
        //回执失败的条数
        int failNum = total - successNum;

        //存放回执成功的记录集合
        List<SendRecord> successList = Lists.newArrayList();
        //存放回执失败的记录集合
        List<SendRecord> failList = Lists.newArrayList();
        //计算成功和失败的集合
        for (int i = 0;i < list.size();i++){
            int random = RandomUtils.nextInt(1,total);
            if (random > successNum){//失败
                failList.add(list.get(i));
                failNum--;
            }else {//成功
                successList.add(list.get(i));
                successNum--;
            }
            total--;
        }

        //批量回执成功
        int success = sendRecordService.batchUpdateMsgReturnState(successList,true);
        //批量回执失败
        int fail = sendRecordService.batchUpdateMsgReturnState(failList,false);
        return String.format(resultMsgFormat,list.size(),success,fail);
    }

    public static void main(String[] args) {
        BigDecimal successRate = new BigDecimal(87.6);
        int total = 232;
        //向上取整
        int successNum  = successRate.divide(new BigDecimal(100)).multiply(new BigDecimal(total)).setScale(0,BigDecimal.ROUND_UP).intValue();
        int failNum = total - successNum;
        System.out.println( total + " " + successNum +  " " + failNum);
        int success = 0;
        int fail = 0;
        int itTotal = total;
        for (int i = 0;i < itTotal;i++){
            int random = RandomUtils.nextInt(1,total+1);
            if (random > successNum){//失败
                fail ++;

                failNum--;
            }else {//成功
                success++;
                successNum--;
            }
            total--;
        }
        System.out.println( success +  " " + fail);
    }

    /**
     * 手动批量修改发送记录的回执状态
     * @param list
     * @param success
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchUpdateMsgReturnState(List<SendRecord> list, Boolean success){
        if (list.size() < 1){
            log.warn("【批量修改回执状态】没有符合条件的记录，更新失败");
            return 0;
        }


        int returnstate = success? CommonStateEnums.SUCCESS.getCode():CommonStateEnums.FAIL.getCode();
        int receiptState = success?SendRecordStateEnums.ReceiptSuccess.getCode():SendRecordStateEnums.ReceiptFail.getCode();

        int size = 1000;//每1000条批量更新和保存一次
        int totalPage = (list.size() + size -1)/size;

        for (int i = 0;i < totalPage;i++){
            List<Long> updateIds = Lists.newArrayList();
            List<ReturnRecord> returnRecords = Lists.newArrayList();
            list.stream().skip(i*size).limit(size).forEach(sendRecord -> {

                updateIds.add(sendRecord.getId());

                ReturnRecord returnRecord = new ReturnRecord();
                returnRecord.setReturnState(returnstate);
                returnRecord.setSmsNumber(sendRecord.getSmsNumber());
                returnRecord.setResMsgid(sendRecord.getResMsgid());
                returnRecord.setCreateTime(new Date());
                returnRecord.setAgentId(sendRecord.getAgentId());
                returnRecord.setChannelId(sendRecord.getChannelId());
                returnRecord.setOrgCode(sendRecord.getOrgCode());
                returnRecord.setReqCreateTime(sendRecord.getCreateTime());
                returnRecord.setReqDesc(sendRecord.getStateDesc());
                returnRecord.setReqState(sendRecord.getState());
                returnRecord.setSendCode(sendRecord.getSendCode());
                returnRecord.setSmsContent(sendRecord.getSmsContent());
                returnRecord.setSmsCount(sendRecord.getSmsCount());
                returnRecord.setSmsNumberArea(sendRecord.getSmsNumberArea());
                returnRecord.setSmsType(sendRecord.getSmsType());
                returnRecord.setSmsWords(sendRecord.getSmsWords());

                returnRecords.add(returnRecord);
            });
            //批量更新发送记录状态
            LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SendRecord::getState, receiptState);
            updateWrapper.set(SendRecord::getReceiptTime, new Date());
            updateWrapper.in(SendRecord::getId, updateIds);
            this.update(updateWrapper);
            //批量新增回执记录
            returnRecordService.saveBatch(returnRecords);
        }
        return list.size();
    }

    @Override
    public void exportSendCordFromAdmin(Long userId, SendRecordQuery sendRecord) {
        //文件名格式   以导出时间_页码为模板命名
        String fileNameTemplate = DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + "_%d.xlsx";
        exportFromAdmin(userId,sendRecord,fileNameTemplate ,1,null);
    }

    @Override
    public String exportSendRecordFromOrg(Long userId, SendRecordQuery sendRecord){
        String fileNameTemplate = "表格%d.xlsx";

        //递归生成所有表格
        Code code = DatabaseCache.getCodeBySortCodeAndCode("System","orgExportPath");
        String dirPath = code.getName() + String.format("%s\\", RandomUtil.randomStringWithDate(18));
        exportFromOrg(dirPath,sendRecord,fileNameTemplate ,1);

        //将生成的表格压缩成zip
        String zipPath = null;
        try {
            zipPath = code.getName() + DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + ".zip";
            //压缩
            FileUtil.compress(dirPath,zipPath);
            //压缩完，删除文件
            FileUtil.delete(dirPath);
            return zipPath;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return zipPath;
    }

    /**
     *  （商户端）递归导出文件
     * @param dirPath
     * @param sendRecord
     * @param fileNameTemplate
     * @param pageNum
     * @return  存放导出文件的目录
     */
    private void exportFromOrg(String dirPath, SendRecordQuery sendRecord,String fileNameTemplate, int pageNum){
        QueryRequest request = new QueryRequest();
        request.setPageNum(pageNum);
        request.setPageSize(20000);

        IPage<SendRecord> result = this.findSendRecords(request, sendRecord);
        if (result != null && result.getTotal() > 0){
            List<SendRecord> sendRecords = result.getRecords();
            List<SendRecordExcel> lst = new ArrayList<>();
            sendRecords.stream().forEach(item -> {
                SendRecordExcel excel = new SendRecordExcel();
                BeanUtils.copyProperties(item,excel);
                //运营商详情
                Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
                String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
                String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
                String operatorInfo = String.format("运营商:%s 地域:%s %s",
                        code != null? code.getName():"未知",
                        StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                        StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
                excel.setOperatorInfo(operatorInfo);
                excel.setReceiptTime(item.getReceiptTime());
                excel.setReturnTime(DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
                //短信详情
                excel.setSmsInfo(String.format("字数:%s 短信数:%s",item.getSmsWords(),item.getSmsCount()));
                lst.add(excel);
            });
            //导出数据
/*            ExportRecord exportRecord = new ExportRecord();
            exportRecord.setFilename(String.format(fileNameTemplate,result.getCurrent()));
            exportRecord.setUserId(userId.intValue());
            exportRecord.setType(ExportTypeEnums.ExportSendRecord.getCode());
            exportRecord.setUserType(UserTypeEnums.Organization.getCode());
            exportRecordService.exportSendRecord(exportRecord,lst);*/
            Code code = DatabaseCache.getCodeBySortCodeAndCode("System","orgExportPath");

            String filePath = dirPath + String.format(fileNameTemplate,result.getCurrent());
            File file = null;
            OutputStream outputStream = null;
            try {
                File dir = new File(dirPath);
                if (!dir.exists()){
                    dir.mkdir();
                }
                file = new File(filePath);
                outputStream = new FileOutputStream(file);
                ExcelKit.$Builder(SendRecordExcel.class, outputStream).writeXlsx(lst, false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //递归创建剩下的表格
            if (result.getCurrent() < result.getPages()){
                exportFromOrg(dirPath,sendRecord,fileNameTemplate,pageNum + 1);
            }

        }

    }


    /**
     * 递归分批导出
     * @param userId
     * @param sendRecord
     * @param fileNameTemplate
     * @param pageNum  外部调用传1，递归调用传下一页码
     * @param totalPage  外部调用任意值，内部会自动赋值
     */
    private void exportFromAdmin(Long userId, SendRecordQuery sendRecord,String fileNameTemplate, Integer pageNum,Long totalPage){

        QueryRequest request = new QueryRequest();
        request.setPageNum(pageNum);
        int exportTotalPage = 20000;
        
        /**
		 * @begin 2021-11-08
		 *  新增导出数量可配置
		 * 
		 */
        Code exportTotalPageCode = DatabaseCache.getCodeBySortCodeAndCode("System","exportTotalPage");
	    if(exportTotalPageCode!=null&&!"".equals(exportTotalPageCode.getName()))
	    {
	    	String exportTotalPageStr = exportTotalPageCode.getName();
	    	try {
	    		exportTotalPage = Integer.valueOf(exportTotalPageStr);
			} catch (Exception e) {
				// TODO: handle exception
			}
	    }
        /**
         * @end
         */
        request.setPageSize(exportTotalPage);

        IPage<SendRecord> result = this.exportFindSendRecords(request, sendRecord);
        if (result != null && result.getRecords().size() > 0){
            //数据库对象转换成  导出对象
            List<SendRecord> sendRecords = result.getRecords();
            List<SendRecordExtExcel> lst = new ArrayList<>();
            sendRecords.stream().forEach(item -> {
                SendRecordExtExcel excel = new SendRecordExtExcel();
                BeanUtils.copyProperties(item,excel);
                //运营商详情
                Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
                String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
                String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
                String operatorInfo = String.format("运营商:%s 地域:%s %s",
                        code != null? code.getName():"未知",
                        StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                        StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
                excel.setOperatorInfo(operatorInfo);
                excel.setOrgCode(item.getOrgCode());
                excel.setReceiptTime(item.getReceiptTime());
                excel.setReturnTime(DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
                //短信详情
                excel.setSmsInfo(String.format("字数:%s 短信数:%s",item.getSmsWords(),item.getSmsCount()));
                lst.add(excel);
            });

            //导出数据
            ExportRecord exportRecord = new ExportRecord();
            exportRecord.setFilename(String.format(fileNameTemplate,result.getCurrent()));
            exportRecord.setUserId(userId.intValue());
            exportRecord.setType(ExportTypeEnums.ExportSendRecord.getCode());
            exportRecord.setUserType(UserTypeEnums.Admin.getCode());
            exportRecordService.exportSendRecord(exportRecord,lst);

            //count查询非常耗时，只需要查询一次  获取 totalPage,剩下的递归次数无需再次查询
            if (sendRecord.getSearchCount() == null || sendRecord.getSearchCount()){
                totalPage = result.getPages();
                sendRecord.setSearchCount(false);
            }
            //递归导出
            if (result.getCurrent() < totalPage){
                exportFromAdmin(userId,sendRecord,fileNameTemplate,pageNum + 1,totalPage);
            }

        }
    }

    @Override
    public int countByQueryEntity(SendRecordQuery sendRecord){
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//商户编码
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//手机号码
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null){//通道
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//状态
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//号码归属地
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getAgentId() != null){
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//短信内容
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        return count(queryWrapper);
    }

    private IPage<SendRecord> exportFindSendRecords(QueryRequest request, SendRecordQuery sendRecord) {
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//商户编码
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//手机号码
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null){//通道
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//状态
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (sendRecord.getAgentId() != null){
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//短信内容
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//号码归属地
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getGroupId() != null){
            queryWrapper.inSql(BaseSend::getOrgCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",sendRecord.getGroupId()));
        }
        // TODO 设置查询条件
        Page<SendRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        if (sendRecord.getSearchCount() != null){
            page.setSearchCount(sendRecord.getSearchCount());
        }
        return this.page(page, queryWrapper);
    }

}
