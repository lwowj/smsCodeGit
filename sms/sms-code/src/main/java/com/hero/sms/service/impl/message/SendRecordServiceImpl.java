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
 * ???????????? Service??????
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
	 * ???????????????????????????????????????????????????
	 * ?????????????????????????????????????????????????????????????????????????????????
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
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//????????????
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//????????????
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        /**
         * 2021-04-17
         * @begin
         */
        if (sendRecord.getSubmitStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecord::getSubmitTime,sendRecord.getSubmitStartTime());
        }
        if (sendRecord.getSubmitEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecord::getSubmitTime,sendRecord.getSubmitEndTime());
        }
        /**
         * @end
         */
        if (sendRecord.getChannelId() != null){//??????
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getUpAgentId() != null){//????????????id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//??????
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getResMsgid())){//????????????
            queryWrapper.eq(SendRecord::getResMsgid,sendRecord.getResMsgid());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (sendRecord.getAgentId() != null){
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//????????????
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//???????????????
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getSmsCount() != null){//????????????
            queryWrapper.eq(SendRecord:: getSmsCount,sendRecord.getSmsCount());
        }
        if (StringUtils.isNotBlank(sendRecord.getStateDesc())){//????????????
            queryWrapper.like(SendRecord::getStateDesc,sendRecord.getStateDesc());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(SendRecord::getSmsNumberArea, sendRecord.getSmsNumberArea());
		}
		if (sendRecord.getGroupId() != null){
            queryWrapper.inSql(BaseSend::getOrgCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",sendRecord.getGroupId()));
        }
        /**
         * @begin 2020-11-17
         * ??????????????????
         */
        //??????id????????????
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
	   	//????????????????????????
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
	   	//???????????????????????????
	   	if (sendRecord.getNotIsConsumeAmount() != null)
	   	{
	   		queryWrapper.ne(SendRecord::getConsumeAmount, sendRecord.getNotIsConsumeAmount());
        }
	   	//????????????
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
	   	 * ??????resid????????????
	   	 */
        //??????resid????????????
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
	   	//???????????????????????????
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
        
        // TODO ??????????????????
        Page<SendRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SendRecord> findSendRecords(SendRecordQuery sendRecord) {
	    LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
	    if (StringUtils.isNotBlank(sendRecord.getBeforeApplyString())){
	        queryWrapper.apply(sendRecord.getBeforeApplyString());
        }
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//????????????
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//????????????
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        /**
         * 2021-04-17
         * @begin
         */
        if (sendRecord.getSubmitStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecord::getSubmitTime,sendRecord.getSubmitStartTime());
        }
        if (sendRecord.getSubmitEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecord::getSubmitTime,sendRecord.getSubmitEndTime());
        }
        /**
         * @end
         */
        if (sendRecord.getChannelId() != null){//??????
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getAgentId() != null){//??????id
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (sendRecord.getUpAgentId() != null){//????????????id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//??????
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//????????????
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecord.getResMsgid())){//????????????
            queryWrapper.eq(SendRecord::getResMsgid,sendRecord.getResMsgid());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//???????????????
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getSmsCount() != null){//????????????
            queryWrapper.eq(SendRecord:: getSmsCount,sendRecord.getSmsCount());
        }
        if (StringUtils.isNotBlank(sendRecord.getStateDesc())){//????????????
            queryWrapper.like(SendRecord::getStateDesc,sendRecord.getStateDesc());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(SendRecord::getSmsNumberArea, sendRecord.getSmsNumberArea());
		}
		if (sendRecord.getGroupId() != null){
            queryWrapper.inSql(BaseSend::getOrgCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",sendRecord.getGroupId()));
        }
        /**
         * @begin 2020-11-17
         * ??????????????????
         */
        //??????id????????????
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
	   	//????????????????????????
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
	   	//???????????????????????????
	   	if (sendRecord.getNotIsConsumeAmount() != null)
	   	{
	   		queryWrapper.ne(SendRecord::getConsumeAmount, sendRecord.getNotIsConsumeAmount());
        }
	   	//????????????
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
	   	 * ??????resid????????????
	   	 */
        //??????resid????????????
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
		//???????????????????????????
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
		// TODO ??????????????????
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
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//????????????
            wrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            wrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//????????????
            wrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            wrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            wrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null){//??????
            wrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getAgentId() != null){//??????id
            wrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (sendRecord.getUpAgentId() != null){//????????????id
            wrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//??????
            wrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            wrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
	    // TODO ??????????????????
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
        	log.error(String.format("???????????????%s??????????????????%s????????????", sendRecordId,sendRecord.getChannelId()));
            return;
        }
        String protocolType = smsChannelExt.getProtocolType();
        if(!SmsChannelProtocolTypeEnums.Http.getCode().equals(protocolType)) {
        	log.error(String.format("???????????????%s??????????????????%s???????????????", sendRecordId,smsChannelExt.getId()));
        	return;
        }
        
        /**
		 * @begin 2021-10-23
		 * ??????????????????????????????????????????
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
 	    		log.error(String.format("???????????????%s??????????????????%s?????????????????????,??????????????????", sendRecordId,sendRecord.getChannelId()));
 	            return;
 	    	}
 	    }
 	   /**
 	    * @end
 	    */
 	    
        /**
		 * @begin 2020-12-21
		 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		 * ??????????????????????????????????????????????????????????????????????????????
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
 	    		 //1???????????????id????????? 0????????? ??????????????????????????????
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
 				log.error(String.format("HTTP???????????????%s???????????????%s???????????????%s?????????????????????", sendRecordId,sendRecord.getSendCode(),sendRecord.getSmsNumber()));
 				try {
 					SendRecord entity = new SendRecord();
 					entity.setId(sendRecord.getId());
 					entity.setStateDesc("????????????????????????????????????");
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
        	log.error(String.format("?????????????????????%d???",sendRecord.getId()),e);
        	sendRecord.setState(SendRecordStateEnums.ReqFail.getCode());
            sendRecord.setStateDesc("??????????????????");
        }

        LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SendRecord::getState, sendRecord.getState());
        updateWrapper.set(SendRecord::getStateDesc, sendRecord.getStateDesc());
        updateWrapper.set(SendRecord::getResMsgid, sendRecord.getResMsgid());
        //2020-12-01 ?????????????????????????????????
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
		//??????ID??????????????????
		SendRecord sendRecord = getById(sendRecordId);
		if(sendRecord == null) return;
		if(sendRecord.getChannelId() == null) return;
		//??????????????????????????????????????????????????????????????????????????????
		if(sendRecord.getState().intValue() != SendRecordStateEnums.WaitReq.getCode() &&  sendRecord.getState().intValue() != SendRecordStateEnums.ReqFail.getCode()) return;
		
		if("ON".equals(showRunTimeLogSwitch)) 
 	    {
			String sendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
			long sendRecordRuntime = DateUtil.getTime(sendRecordBeginTime, sendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
			log.error(String.format("???1?????????????????????sendRecord????????????%s??????%s??????%s???:???????????????%s??????????????????%s????????????%s?????????",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),sendRecordBeginTime,sendRecordEndTime,String.valueOf(sendRecordRuntime)));
 	    }
		
	    String findChannelBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
	    
	    /**
		 * @begin 2022-05-02
		 * ????????????????????????????????????
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
        	log.error(String.format("???????????????%s??????????????????%s????????????", sendRecordId,sendRecord.getChannelId()));
            return;
        }
		/**
		 * @begin 2021-10-23
		 * ??????????????????????????????????????????
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
 	    		log.error(String.format("???????????????%s??????????????????%s?????????????????????,??????????????????", sendRecordId,sendRecord.getChannelId()));
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
			log.error(String.format("???2?????????????????????smsChannelExt???????????????????????????%s??????%s??????%s??????%s???:???????????????%s??????????????????%s????????????%s?????????",querySmppChannelWay,sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),findChannelBeginTime,findChannelEndTime,String.valueOf(findChannelRuntime)));
 	    }
		
		/**
		 * @begin 2020-12-21
		 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		 * ??????????????????????????????????????????????????????????????????????????????
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
 	    		 //1???????????????id????????? 0????????? ??????????????????????????????
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
 					log.error(String.format("???3?????????????????????????????????????????????%s??????%s??????%s???:???????????????%s??????????????????%s????????????%s?????????",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),checkSendRecordBeginTime,checkSendRecordEndTime,String.valueOf(checkSendRecordRuntime)));
 		 	    }
 			} catch (Exception e) {
 				// TODO: handle exception
 				log.error(String.format("SMPP???????????????%s???????????????%s???????????????%s?????????????????????", sendRecordId,sendRecord.getSendCode(),sendRecord.getSmsNumber()));
 				String updateSendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 				try {
 					SendRecord entity = new SendRecord();
 					entity.setId(sendRecord.getId());
 					entity.setStateDesc("????????????????????????????????????");
 					sendRecordService.updateById(entity);
 				} catch (Exception e2) {}
 				
 				if("ON".equals(showRunTimeLogSwitch)) 
 		 	    {
	 				String updateSendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
	 				long updateSendRecordRuntime = DateUtil.getTime(updateSendRecordBeginTime, updateSendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
	 			    log.error(String.format("???4????????????????????????????????????????????????%s??????%s??????%s???:???????????????%s??????????????????%s????????????%s?????????",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),updateSendRecordBeginTime,updateSendRecordEndTime,String.valueOf(updateSendRecordRuntime)));
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
        	//3-1-2-2-1 ??????????????????????????????
        	String pushBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
        	/**
        	 * @begin 2022-05-09
        	 * ????????????????????????????????????????????????
        	 * ??????????????????  ON:???????????????????????? ??? OFF???????????? ??? ??????????????????
        	 * ??????????????????????????????70????????????????????????????????????
        	 */
        	//sendSmsWayAsynFlag????????????????????????
        	boolean sendSmsWayAsynFlag = false; 
        	
        	String sendSmsWaySwitch = "OFF";
    		Code sendSmsWaySwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendSmsWaySwitch");
    	    if(sendSmsWaySwitchCode!=null&&!"".equals(sendSmsWaySwitchCode.getName()))
    	    {
    	    	sendSmsWaySwitch = sendSmsWaySwitchCode.getName();
    	    }
    	    //???????????????????????????????????????????????????????????????????????????
    	    //submitWay  0?????? 1??????
    	    if("ON".equals(sendSmsWaySwitch))
    	    {
    	    	int submitWay = 0;
    	    	try {
    	    		submitWay = smsChannel.getSubmitWay();
				} catch (Exception e) {}
    	    	if(submitWay == SmsChannelSubmitWayEnums.ASYN.getCode())
    	    	{
    	    		//??????????????????????????????70????????????????????????????????????
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
    	    //sendSmsWayAsynFlag ??????????????????????????????????????????true ?????????false ??????
    	    String sendSmsWayString = "";
    	    if(sendSmsWayAsynFlag)
    	    {
    	    	sendSmsWayString = "????????????";
    	    	pushService.smppPushAsyn(sendRecord);
    	    }
    	    else
    	    {
    	    	sendSmsWayString = "????????????";
    	    	pushService.smppPush(sendRecord);
    	    }
    	    /**
    	     * @end
    	     */
        	if("ON".equals(showRunTimeLogSwitch)) 
        	{
 				String pushEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 				long pushRuntime = DateUtil.getTime(pushBeginTime, pushEndTime,DateUtil.Y_M_D_H_M_S_S_2);
 			    log.error(String.format("???5???????????????????????????????????????%s??????%s??????%s??????%s???:???????????????%s??????????????????%s????????????%s?????????",sendSmsWayString,sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),pushBeginTime,pushEndTime,String.valueOf(pushRuntime)));
		 	}
        }
        else
        {
        	log.error(String.format("???????????????%s??????????????????%s???????????????", sendRecordId,smsChannel.getId()));
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
			log.error(String.format("???SMPP?????????????????????%s??????%s??????%s???:???????????????%s??????????????????%s????????????%s?????????",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),pushMsgBeginTime,pushMsgEndTime,String.valueOf(pushMsgRuntime)));
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
	 * ????????????id????????????????????? ??????????????????
	 */
	@Override
	public SendRecord getByChannelIdAndMsgId(int channelId, String resMsgid) {
		LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SendRecord::getChannelId, channelId);
		queryWrapper.eq(SendRecord::getResMsgid, resMsgid);
		return getOne(queryWrapper, true);
	}
    /**
     * ?????????????????????
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
     * ?????????????????????
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

    //????????????????????????
    public int batchNotifyMsgState(List<SendRecord> list){
        AtomicInteger count = new AtomicInteger();
        //???????????????
        Map<String,List<SendRecord>> orgMap = list.stream()
                .collect(Collectors.groupingBy(SendRecord::getOrgCode));
        //????????????
        Set<Map.Entry<String, List<SendRecord>>> entrysets = orgMap.entrySet();
        entrysets.stream().forEach(stringListEntry -> {
            String orgCode = stringListEntry.getKey();
            Organization org = organizationService.getOrganizationByCode(orgCode);
            if (org == null){
                log.error(String.format("?????????%s????????????????????????????????????",orgCode));
                return;
            }
            /*if (StringUtils.isBlank(org.getNotifyUrl())){
                log.error(String.format("?????????%s???????????????????????????????????????????????????",orgCode));
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
                    log.error(String.format("???????????????id:%s?????????????????????",sendRecord.getId()),e);
                }
            });
        });
        return count.intValue();
    }

    @Override
    public int batchUpdateMsgReturnState(String sendRecordIds, Boolean success) {

        if (StringUtils.isBlank(sendRecordIds) || success == null ){
            log.warn("??????????????????????????????????????????????????????????????????");
            return 0;
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        //???????????????????????????????????????????????????????????????
        queryWrapper.apply("`state` = 4")
                .in(SendRecord::getId,ids);
        List<SendRecord> list = list(queryWrapper);

        return sendRecordService.batchUpdateMsgReturnState(list,success);
    }

    @Override
    public String batchUpdateMsgReturnState(SendRecordQuery sendRecordQuery, BigDecimal successRate) {
        String resultMsgFormat = "???????????????%s???????????????????????????????????????%s??????????????????%s????????????";
        if (sendRecordQuery == null || successRate == null ){
            log.warn("??????????????????????????????????????????????????????????????????");
            return String.format(resultMsgFormat,0,0,0);
        }
        sendRecordQuery.setBeforeApplyString("`state` = 4");
        List<SendRecord> list = this.findSendRecords(sendRecordQuery);
        int total = list.size();
        //?????????????????????
        int successNum  = successRate.divide(new BigDecimal(100)).multiply(new BigDecimal(total)).setScale(0,BigDecimal.ROUND_UP).intValue();
        //?????????????????????
        int failNum = total - successNum;

        //?????????????????????????????????
        List<SendRecord> successList = Lists.newArrayList();
        //?????????????????????????????????
        List<SendRecord> failList = Lists.newArrayList();
        //??????????????????????????????
        for (int i = 0;i < list.size();i++){
            int random = RandomUtils.nextInt(1,total);
            if (random > successNum){//??????
                failList.add(list.get(i));
                failNum--;
            }else {//??????
                successList.add(list.get(i));
                successNum--;
            }
            total--;
        }

        //??????????????????
        int success = sendRecordService.batchUpdateMsgReturnState(successList,true);
        //??????????????????
        int fail = sendRecordService.batchUpdateMsgReturnState(failList,false);
        return String.format(resultMsgFormat,list.size(),success,fail);
    }

    public static void main(String[] args) {
        BigDecimal successRate = new BigDecimal(87.6);
        int total = 232;
        //????????????
        int successNum  = successRate.divide(new BigDecimal(100)).multiply(new BigDecimal(total)).setScale(0,BigDecimal.ROUND_UP).intValue();
        int failNum = total - successNum;
        System.out.println( total + " " + successNum +  " " + failNum);
        int success = 0;
        int fail = 0;
        int itTotal = total;
        for (int i = 0;i < itTotal;i++){
            int random = RandomUtils.nextInt(1,total+1);
            if (random > successNum){//??????
                fail ++;

                failNum--;
            }else {//??????
                success++;
                successNum--;
            }
            total--;
        }
        System.out.println( success +  " " + fail);
    }

    /**
     * ?????????????????????????????????????????????
     * @param list
     * @param success
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchUpdateMsgReturnState(List<SendRecord> list, Boolean success){
        if (list.size() < 1){
            log.warn("????????????????????????????????????????????????????????????????????????");
            return 0;
        }


        int returnstate = success? CommonStateEnums.SUCCESS.getCode():CommonStateEnums.FAIL.getCode();
        int receiptState = success?SendRecordStateEnums.ReceiptSuccess.getCode():SendRecordStateEnums.ReceiptFail.getCode();

        int size = 1000;//???1000??????????????????????????????
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
            //??????????????????????????????
            LambdaUpdateWrapper<SendRecord> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SendRecord::getState, receiptState);
            updateWrapper.set(SendRecord::getReceiptTime, new Date());
            updateWrapper.in(SendRecord::getId, updateIds);
            this.update(updateWrapper);
            //????????????????????????
            returnRecordService.saveBatch(returnRecords);
        }
        return list.size();
    }

    @Override
    public void exportSendCordFromAdmin(Long userId, SendRecordQuery sendRecord) {
        //???????????????   ???????????????_?????????????????????
        String fileNameTemplate = DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + "_%d.xlsx";
        exportFromAdmin(userId,sendRecord,fileNameTemplate ,1,null);
    }

    @Override
    public String exportSendRecordFromOrg(Long userId, SendRecordQuery sendRecord){
        String fileNameTemplate = "??????%d.xlsx";

        //????????????????????????
        Code code = DatabaseCache.getCodeBySortCodeAndCode("System","orgExportPath");
        String dirPath = code.getName() + String.format("%s\\", RandomUtil.randomStringWithDate(18));
        exportFromOrg(dirPath,sendRecord,fileNameTemplate ,1);

        //???????????????????????????zip
        String zipPath = null;
        try {
            zipPath = code.getName() + DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + ".zip";
            //??????
            FileUtil.compress(dirPath,zipPath);
            //????????????????????????
            FileUtil.delete(dirPath);
            return zipPath;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return zipPath;
    }

    /**
     *  ?????????????????????????????????
     * @param dirPath
     * @param sendRecord
     * @param fileNameTemplate
     * @param pageNum
     * @return  ???????????????????????????
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
                //???????????????
                Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
                String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
                String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
                String operatorInfo = String.format("?????????:%s ??????:%s %s",
                        code != null? code.getName():"??????",
                        StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"??????" ,
                        StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"??????");
                excel.setOperatorInfo(operatorInfo);
                excel.setReceiptTime(item.getReceiptTime());
                excel.setReturnTime(DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
                //????????????
                excel.setSmsInfo(String.format("??????:%s ?????????:%s",item.getSmsWords(),item.getSmsCount()));
                lst.add(excel);
            });
            //????????????
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
            //???????????????????????????
            if (result.getCurrent() < result.getPages()){
                exportFromOrg(dirPath,sendRecord,fileNameTemplate,pageNum + 1);
            }

        }

    }


    /**
     * ??????????????????
     * @param userId
     * @param sendRecord
     * @param fileNameTemplate
     * @param pageNum  ???????????????1??????????????????????????????
     * @param totalPage  ?????????????????????????????????????????????
     */
    private void exportFromAdmin(Long userId, SendRecordQuery sendRecord,String fileNameTemplate, Integer pageNum,Long totalPage){

        QueryRequest request = new QueryRequest();
        request.setPageNum(pageNum);
        int exportTotalPage = 20000;
        
        /**
		 * @begin 2021-11-08
		 *  ???????????????????????????
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
            //????????????????????????  ????????????
            List<SendRecord> sendRecords = result.getRecords();
            List<SendRecordExtExcel> lst = new ArrayList<>();
            sendRecords.stream().forEach(item -> {
                SendRecordExtExcel excel = new SendRecordExtExcel();
                BeanUtils.copyProperties(item,excel);
                //???????????????
                Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
                String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
                String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
                String operatorInfo = String.format("?????????:%s ??????:%s %s",
                        code != null? code.getName():"??????",
                        StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"??????" ,
                        StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"??????");
                excel.setOperatorInfo(operatorInfo);
                excel.setOrgCode(item.getOrgCode());
                excel.setReceiptTime(item.getReceiptTime());
                excel.setReturnTime(DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
                //????????????
                excel.setSmsInfo(String.format("??????:%s ?????????:%s",item.getSmsWords(),item.getSmsCount()));
                lst.add(excel);
            });

            //????????????
            ExportRecord exportRecord = new ExportRecord();
            exportRecord.setFilename(String.format(fileNameTemplate,result.getCurrent()));
            exportRecord.setUserId(userId.intValue());
            exportRecord.setType(ExportTypeEnums.ExportSendRecord.getCode());
            exportRecord.setUserType(UserTypeEnums.Admin.getCode());
            exportRecordService.exportSendRecord(exportRecord,lst);

            //count??????????????????????????????????????????  ?????? totalPage,???????????????????????????????????????
            if (sendRecord.getSearchCount() == null || sendRecord.getSearchCount()){
                totalPage = result.getPages();
                sendRecord.setSearchCount(false);
            }
            //????????????
            if (result.getCurrent() < totalPage){
                exportFromAdmin(userId,sendRecord,fileNameTemplate,pageNum + 1,totalPage);
            }

        }
    }

    @Override
    public int countByQueryEntity(SendRecordQuery sendRecord){
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//????????????
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//????????????
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null){//??????
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getUpAgentId() != null){//????????????id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//??????
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//???????????????
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getAgentId() != null){
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//????????????
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        return count(queryWrapper);
    }

    private IPage<SendRecord> exportFindSendRecords(QueryRequest request, SendRecordQuery sendRecord) {
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){//????????????
            queryWrapper.eq(BaseSend::getOrgCode,sendRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumber())) {//????????????
            queryWrapper.eq(SendRecord::getSmsNumber,sendRecord.getSmsNumber());
        }
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null){//??????
            queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
        }
        if (sendRecord.getUpAgentId() != null){//????????????id
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
        }
        if (sendRecord.getState() != null){//??????
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (sendRecord.getAgentId() != null){
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//????????????
            queryWrapper.like(SendRecord::getSmsContent,sendRecord.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince())){//???????????????
            queryWrapper.eq(SendRecord::getSmsNumberProvince,sendRecord.getSmsNumberProvince());
        }
        if (sendRecord.getGroupId() != null){
            queryWrapper.inSql(BaseSend::getOrgCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",sendRecord.getGroupId()));
        }
        // TODO ??????????????????
        Page<SendRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        if (sendRecord.getSearchCount() != null){
            page.setSearchCount(sendRecord.getSearchCount());
        }
        return this.page(page, queryWrapper);
    }

}
