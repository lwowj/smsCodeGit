package com.hero.sms.service.impl.message;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.ReturnRecordQuery;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.entity.message.history.ReturnRecordHistory;
import com.hero.sms.entity.message.history.ReturnRecordHistoryQuery;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;
import com.hero.sms.entity.message.history.SendRecordHistory;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.mapper.message.ReportMapper;
import com.hero.sms.service.message.IReportService;
@Service
public class ReportServiceImpl implements IReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<Map<String, Object>> orgSendCountToday() {
        List<Map<String, Object>> list = reportMapper.statisticOrgSendCountToday();
        list.stream().forEach(stringObjectMap -> {
            String orgCode = (String) stringObjectMap.get("orgCode");
            String orgName = "";
            if (StringUtils.isNotBlank(orgCode)){
                orgName = DatabaseCache.getOrgNameByOrgcode(orgCode);
            }
            stringObjectMap.put("orgName",orgName);
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> provinceSendCountToday() {
        List<Map<String, Object>> list = reportMapper.provinceSendCountToday();
        list.stream().forEach(stringObjectMap -> {
            String province = (String) stringObjectMap.get("province");
            String provinceName = "未知";
            if (StringUtils.isNotBlank(province)){
                provinceName = SmsNumberAreaProvinceEnums.getNameByCode(province);
            }
            stringObjectMap.put("provinceName",provinceName);
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> operatorSendCountToday() {
        List<Map<String, Object>> list = reportMapper.operatorSendCountToday();
        list.stream().forEach(stringObjectMap -> {
            String operator = (String) stringObjectMap.get("operator");
            String operatorName = "未知";
            if (StringUtils.isNotBlank(operator)){
                Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator", operator);
                if (code != null){
                    operatorName = code.getName();
                }
            }
            stringObjectMap.put("operatorName",operatorName);
        });

        return list;
    }

    @Override
    public Map<String, Object> statisticSendBoxInfo(SendBoxQuery sendBox) {
        LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(sendBox.getIsTiming() != null) {//是否定时
            if(sendBox.getIsTiming())queryWrapper.isNotNull(SendBox::getTimingTime);
            else queryWrapper.isNull(SendBox::getTimingTime);
        }
        if(sendBox.getLeTimingTime() != null) {//定时时间
            queryWrapper.le(SendBox::getTimingTime, sendBox.getLeTimingTime());
        }
        if (sendBox.getSortingStartTime() != null){//分拣开始时间
            queryWrapper.ge(SendBox::getSortingTime,sendBox.getSortingStartTime());
        }
        if (sendBox.getSortingEndTime() != null) {// 分拣结束时间
            queryWrapper.le(SendBox::getSortingTime, sendBox.getSortingEndTime());
        }
        if (StringUtils.isNotBlank(sendBox.getSendCode())) {// 批次号
            queryWrapper.eq(BaseSend::getSendCode, sendBox.getSendCode());
        }
        if (StringUtils.isNotBlank(sendBox.getCreateUsername())) {// 提交人
            queryWrapper.eq(SendBox::getCreateUsername, sendBox.getCreateUsername());
        }
        if (sendBox.getAgentId() != null) {// 代理商id
            queryWrapper.eq(BaseSend::getAgentId, sendBox.getAgentId());
        }
        if (sendBox.getUpAgentId() != null){//上级代理
            queryWrapper.eq(SendBox::getUpAgentId,sendBox.getUpAgentId());
        }
        if (StringUtils.isNotBlank(sendBox.getOrgCode())) {// 商户编码
            queryWrapper.eq(BaseSend::getOrgCode, sendBox.getOrgCode());
        }
        if (sendBox.getSmsType() != null) {// 短信类型
            queryWrapper.eq(BaseSend::getSmsType, sendBox.getSmsType());
        }
        if (sendBox.getAuditState() != null) {// 审核状态
            queryWrapper.eq(SendBox::getAuditState, sendBox.getAuditState());
        }
        if (sendBox.getSubType() != null) {// 提交方式
            queryWrapper.eq(SendBox::getSubType, sendBox.getSubType());
        }
        if (sendBox.getCreateStartTime() != null){
            queryWrapper.ge(SendBox::getCreateTime,sendBox.getCreateStartTime());
        }
        if (sendBox.getCreateEndTime() != null){
            queryWrapper.le(SendBox::getCreateTime,sendBox.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendBox.getSmsNumbers())){
            queryWrapper.like(SendBox::getSmsNumbers,sendBox.getSmsNumbers());
        }
        if (StringUtils.isNotBlank(sendBox.getSmsContent())){
            queryWrapper.like(BaseSend::getSmsContent,sendBox.getSmsContent());
        }
        if (sendBox.getExcludeAuditState() != null){
            queryWrapper.ne(SendBox::getAuditState,sendBox.getExcludeAuditState());
        }
        /**
		 * 2021-01-28
		 */
        if(sendBox.getIsTimingFlag() != null) {//是否定时(用于查询)
	    	if(sendBox.getIsTimingFlag())queryWrapper.isNotNull(SendBox::getIsTimingTime);
	    	else queryWrapper.isNull(SendBox::getIsTimingTime);
	    }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendBox.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(BaseSend::getSmsNumberArea, sendBox.getSmsNumberArea());
		}
		/**
	     * 是否长短信
	     * 1 不是
	     * 2 是
	     */
		if (sendBox.getIsLongSms()!= null){//是否长短信
			int wordsOfPerMsgInt = 70;
			Code wordsOfPerMsgCode = DatabaseCache.getCodeBySortCodeAndCode("System","wordsOfPerMsg");
		    if(wordsOfPerMsgCode!=null&&!"".equals(wordsOfPerMsgCode.getName()))
		    {
		    	try {
		    		wordsOfPerMsgInt = Integer.parseInt(wordsOfPerMsgCode.getName());
				} catch (Exception e) {}
		    }
			if(sendBox.getIsLongSms()==1)
			{
				//非长短信，字数小于等于70
				queryWrapper.le(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
			else
			{
				//大于70
				queryWrapper.gt(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
		}
		/**
		 * 2021-03-29
		 * 是否已分拣
		 */
		if (sendBox.getIsSortingFlag() != null) {
			if (sendBox.getIsSortingFlag())
				queryWrapper.isNotNull(SendBox::getSortingTime);
			else
				queryWrapper.isNull(SendBox::getSortingTime);
		}
        return reportMapper.statisticSendBoxInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticSendBoxHistoryInfo(SendBoxHistoryQuery sendBoxHistory) {
        LambdaQueryWrapper<SendBoxHistory> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(sendBoxHistory.getIsTiming() != null) {//是否定时
            if(sendBoxHistory.getIsTiming())queryWrapper.isNotNull(SendBoxHistory::getTimingTime);
            else queryWrapper.isNull(SendBoxHistory::getTimingTime);
        }
        if(sendBoxHistory.getLeTimingTime() != null) {//定时时间
            queryWrapper.ge(SendBoxHistory::getTimingTime, sendBoxHistory.getLeTimingTime());
        }
        if (sendBoxHistory.getSortingStartTime() != null){//分拣开始时间
            queryWrapper.ge(SendBoxHistory::getSortingTime,sendBoxHistory.getSortingStartTime());
        }
        if (sendBoxHistory.getSortingEndTime() != null) {// 分拣结束时间
            queryWrapper.le(SendBoxHistory::getSortingTime, sendBoxHistory.getSortingEndTime());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSendCode())) {// 批次号
            queryWrapper.eq(SendBoxHistory::getSendCode, sendBoxHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getCreateUsername())) {// 提交人
            queryWrapper.eq(SendBoxHistory::getCreateUsername, sendBoxHistory.getCreateUsername());
        }
        if (sendBoxHistory.getAgentId() != null) {// 代理商id
            queryWrapper.eq(SendBoxHistory::getAgentId, sendBoxHistory.getAgentId());
        }
        if (sendBoxHistory.getUpAgentId() != null){//上级代理
            queryWrapper.eq(SendBoxHistory::getUpAgentId,sendBoxHistory.getUpAgentId());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getOrgCode())) {// 商户编码
            queryWrapper.eq(SendBoxHistory::getOrgCode, sendBoxHistory.getOrgCode());
        }
        if (sendBoxHistory.getSmsType() != null) {// 短信类型
            queryWrapper.eq(SendBoxHistory::getSmsType, sendBoxHistory.getSmsType());
        }
        if (sendBoxHistory.getAuditState() != null) {// 审核状态
            queryWrapper.eq(SendBoxHistory::getAuditState, sendBoxHistory.getAuditState());
        }
        if (sendBoxHistory.getSubType() != null) {// 提交方式
            queryWrapper.eq(SendBoxHistory::getSubType, sendBoxHistory.getSubType());
        }
        if (sendBoxHistory.getCreateStartTime() != null){
            queryWrapper.ge(SendBoxHistory::getCreateTime,sendBoxHistory.getCreateStartTime());
        }
        if (sendBoxHistory.getCreateEndTime() != null){
            queryWrapper.le(SendBoxHistory::getCreateTime,sendBoxHistory.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSmsNumbers())){
            queryWrapper.like(SendBoxHistory::getSmsNumbers,sendBoxHistory.getSmsNumbers());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSmsContent())){
            queryWrapper.like(SendBoxHistory::getSmsContent,sendBoxHistory.getSmsContent());
        }
        /**
		 * 2021-01-28
		 */
        if(sendBoxHistory.getIsTimingFlag() != null) {//是否定时(用于查询)
	    	if(sendBoxHistory.getIsTimingFlag())queryWrapper.isNotNull(SendBoxHistory::getIsTimingTime);
	    	else queryWrapper.isNull(SendBoxHistory::getIsTimingTime);
	    }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendBoxHistory.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(SendBoxHistory::getSmsNumberArea, sendBoxHistory.getSmsNumberArea());
		}
		/**
	     * 是否长短信
	     * 1 不是
	     * 2 是
	     */
		if (sendBoxHistory.getIsLongSms()!= null){//是否长短信
			int wordsOfPerMsgInt = 70;
			Code wordsOfPerMsgCode = DatabaseCache.getCodeBySortCodeAndCode("System","wordsOfPerMsg");
		    if(wordsOfPerMsgCode!=null&&!"".equals(wordsOfPerMsgCode.getName()))
		    {
		    	try {
		    		wordsOfPerMsgInt = Integer.parseInt(wordsOfPerMsgCode.getName());
				} catch (Exception e) {}
		    }
			if(sendBoxHistory.getIsLongSms()==1)
			{
				//非长短信，字数小于等于70
				queryWrapper.le(SendBoxHistory::getSmsWords,wordsOfPerMsgInt);
			}
			else
			{
				//大于70
				queryWrapper.gt(SendBoxHistory::getSmsWords,wordsOfPerMsgInt);
			}
		}
        return this.reportMapper.statisticSendBoxHistoryInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticSendRecordInfo(SendRecordQuery sendRecord) {
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
        if (sendRecord.getState() != null){//状态
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (sendRecord.getAgentId() != null){//代理商
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (sendRecord.getUpAgentId() != null){//上级代理商
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
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
	   	 * @begin
	   	 * 2022-05-10
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
        return this.reportMapper.statisticSendRecordInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticSendRecordHistoryInfo(SendRecordHistoryQuery sendRecordHistory) {
        LambdaQueryWrapper<SendRecordHistory> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(sendRecordHistory.getOrgCode())){//商户编码
            queryWrapper.eq(SendRecordHistory::getOrgCode,sendRecordHistory.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSendCode())){//批次号
            queryWrapper.eq(SendRecordHistory::getSendCode,sendRecordHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumber())) {//手机号码
            queryWrapper.eq(SendRecordHistory::getSmsNumber,sendRecordHistory.getSmsNumber());
        }
        if (sendRecordHistory.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(SendRecordHistory::getCreateTime,sendRecordHistory.getCreateStartTime());
        }
        if (sendRecordHistory.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(SendRecordHistory::getCreateTime,sendRecordHistory.getCreateEndTime());
        }
        /**
         * 2021-04-17
         * @begin
         */
        if (sendRecordHistory.getSubmitStartTime() != null){//起始时间 for 提交时间
            queryWrapper.ge(SendRecordHistory::getSubmitTime,sendRecordHistory.getSubmitStartTime());
        }
        if (sendRecordHistory.getSubmitEndTime() != null){//结束时间 for 提交时间
            queryWrapper.le(SendRecordHistory::getSubmitTime,sendRecordHistory.getSubmitEndTime());
        }
        /**
         * @end
         */
        if (sendRecordHistory.getChannelId() != null){//通道
            queryWrapper.eq(SendRecordHistory::getChannelId,sendRecordHistory.getChannelId());
        }
        if (sendRecordHistory.getState() != null){//状态
            queryWrapper.eq(SendRecordHistory::getState,sendRecordHistory.getState());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecordHistory::getSmsNumberOperator,sendRecordHistory.getSmsNumberOperator());
        }
        if (sendRecordHistory.getAgentId() != null){
            queryWrapper.eq(SendRecordHistory::getAgentId,sendRecordHistory.getAgentId());
        }
        if (sendRecordHistory.getUpAgentId() != null){//上级代理商
            queryWrapper.eq(SendRecordHistory::getUpAgentId,sendRecordHistory.getUpAgentId());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsContent())){//短信内容
            queryWrapper.like(SendRecordHistory::getSmsContent,sendRecordHistory.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberProvince())){//号码归属地
            queryWrapper.eq(SendRecordHistory::getSmsNumberProvince,sendRecordHistory.getSmsNumberProvince());
        }
        if (sendRecordHistory.getSmsCount() != null){//有效短信
            queryWrapper.eq(SendRecordHistory:: getSmsCount,sendRecordHistory.getSmsCount());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(SendRecordHistory::getSmsNumberArea, sendRecordHistory.getSmsNumberArea());
		}
        if (StringUtils.isNotBlank(sendRecordHistory.getResMsgid())){//通道标识
            queryWrapper.eq(SendRecordHistory::getResMsgid,sendRecordHistory.getResMsgid());
        }
        return this.reportMapper.statisticSendRecordHistoryInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticReturnRecordInfo(ReturnRecordQuery returnRecord) {
        LambdaQueryWrapper<ReturnRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(returnRecord.getSendCode())){//批次号
            queryWrapper.eq(ReturnRecord::getSendCode,returnRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(returnRecord.getOrgCode())){//商户编码
            queryWrapper.eq(ReturnRecord::getOrgCode,returnRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnRecord.getSmsNumber())){//手机号码
            queryWrapper.eq(ReturnRecord::getSmsNumber,returnRecord.getSmsNumber());
        }
        if (StringUtils.isNotBlank(returnRecord.getResMsgid())){//通道消息标识
            queryWrapper.eq(ReturnRecord::getResMsgid,returnRecord.getResMsgid());
        }
        if (returnRecord.getReqCreateStartTime() != null){//开始时间  for 提交时间
            queryWrapper.ge(ReturnRecord::getReqCreateTime,returnRecord.getReqCreateStartTime());
        }
        if (returnRecord.getReqCreateEndTime() != null){//结束时间  for 提交时间
            queryWrapper.le(ReturnRecord::getReqCreateTime,returnRecord.getReqCreateEndTime());
        }
        if (returnRecord.getChannelId() != null){//通道ID
            queryWrapper.eq(ReturnRecord::getChannelId,returnRecord.getChannelId());
        }
        if (returnRecord.getSmsType() != null){//消息类型
            queryWrapper.eq(ReturnRecord::getSmsType,returnRecord.getSmsType());
        }
        if (returnRecord.getReturnState() != null){//接收状态
            queryWrapper.eq(ReturnRecord::getReturnState,returnRecord.getReturnState());
        }
        if (returnRecord.getReqState() != null){//请求状态
            queryWrapper.eq(ReturnRecord::getReqState,returnRecord.getReqState());
        }
        if (returnRecord.getAgentId() != null){//代理商ID
            queryWrapper.eq(ReturnRecord::getAgentId,returnRecord.getAgentId());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(returnRecord.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(ReturnRecord::getSmsNumberArea, returnRecord.getSmsNumberArea());
		}
        return this.reportMapper.statisticReturnRecordInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticReturnRecordHistoryInfo(ReturnRecordHistoryQuery returnRecordHistory) {
        LambdaQueryWrapper<ReturnRecordHistory> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(returnRecordHistory.getSendCode())){//批次号
            queryWrapper.eq(ReturnRecordHistory::getSendCode,returnRecordHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getOrgCode())){//商户编码
            queryWrapper.eq(ReturnRecordHistory::getOrgCode,returnRecordHistory.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getSmsNumber())){//手机号码
            queryWrapper.eq(ReturnRecordHistory::getSmsNumber,returnRecordHistory.getSmsNumber());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getResMsgid())){//通道消息标识
            queryWrapper.eq(ReturnRecordHistory::getResMsgid,returnRecordHistory.getResMsgid());
        }
        if (returnRecordHistory.getReqCreateStartTime() != null){//开始时间  for 提交时间
            queryWrapper.ge(ReturnRecordHistory::getReqCreateTime,returnRecordHistory.getReqCreateStartTime());
        }
        if (returnRecordHistory.getReqCreateEndTime() != null){//结束时间  for 提交时间
            queryWrapper.le(ReturnRecordHistory::getReqCreateTime,returnRecordHistory.getReqCreateEndTime());
        }
        if (returnRecordHistory.getChannelId() != null){//通道ID
            queryWrapper.eq(ReturnRecordHistory::getChannelId,returnRecordHistory.getChannelId());
        }
        if (returnRecordHistory.getSmsType() != null){//消息类型
            queryWrapper.eq(ReturnRecordHistory::getSmsType,returnRecordHistory.getSmsType());
        }
        if (returnRecordHistory.getReturnState() != null){//接收状态
            queryWrapper.eq(ReturnRecordHistory::getReturnState,returnRecordHistory.getReturnState());
        }
        if (returnRecordHistory.getReqState() != null){//请求状态
            queryWrapper.eq(ReturnRecordHistory::getReqState,returnRecordHistory.getReqState());
        }
        if (returnRecordHistory.getAgentId() != null){//代理商ID
            queryWrapper.eq(ReturnRecordHistory::getAgentId,returnRecordHistory.getAgentId());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(returnRecordHistory.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(ReturnRecordHistory::getSmsNumberArea, returnRecordHistory.getSmsNumberArea());
		}
        return this.reportMapper.statisticReturnRecordHistoryInfo(queryWrapper);
    }

    /**
     * 代理后台实时统计订单情况
     * @param agent
     * @return
     */
    @Override
    public List<Map<String,Object>> sumSendRecordByAgent(Agent agent){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("Agent_Id",agent.getId());
        queryWrapper.ge("create_time", DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        queryWrapper.apply("`state`&62= `state`");
        queryWrapper.groupBy("org_code");
        return this.reportMapper.sumSendRecordByAgent(queryWrapper);
    }

    /**
     * 代理后台实时统计下级代理订单情况
     * @param agent
     * @return
     */
    @Override
    public List<Map<String,Object>> sumLowerSendRecordByAgent(Agent agent){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("`up_agent_id`",agent.getId());
        queryWrapper.ge("create_time", DateUtil.getString(new Date(),"yyyy-MM-dd 00:00:00"));
        queryWrapper.apply("`state`&62= `state`");
        queryWrapper.groupBy("org_code");
        return this.reportMapper.sumSendRecordByAgent(queryWrapper);
    }

    @Override
    public IPage<Map<String, Object>> statisticRateSuccessGroupBySendCode(QueryRequest request, SendRecordQuery sendRecord) {
        QueryWrapper queryWrapper = new QueryWrapper();
        //LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (sendRecord.getAgentId() != null){
            //queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
            queryWrapper.eq("r.agent_id",sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){
            //queryWrapper.eq(SendRecord::getOrgCode,sendRecord.getOrgCode());
            queryWrapper.eq("r.org_code",sendRecord.getOrgCode());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null ){//通道
            //queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
            queryWrapper.eq("r.channel_id",sendRecord.getChannelId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        /**
         * 2021-04-09
         */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince()) ){//号码归属地
            queryWrapper.eq("r.sms_number_province",sendRecord.getSmsNumberProvince());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            //queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
            queryWrapper.eq("r.send_code",sendRecord.getSendCode());
        }
        if(sendRecord.getSubTypeWith() != null) {
    		queryWrapper.apply("r.sub_type&{0}=r.sub_type", sendRecord.getSubTypeWith());
    	}
        queryWrapper.apply("r.`state`& 52= r.`state`");
        //queryWrapper.groupBy(BaseSend::getSendCode);
        queryWrapper.groupBy("r.send_code");
        //queryWrapper.orderByDesc(SendRecord::getCreateTime);
        queryWrapper.orderByDesc("r.create_time");
        if (sendRecord.getSubmitSuccessCount() != null){
            queryWrapper.having("total > {0}",sendRecord.getSubmitSuccessCount());
        }
        Page<Map<String, Object>> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Map<String, Object>> result = this.reportMapper.statisticRateSuccessGroupBySendCode(page, queryWrapper,sendRecord.getGroupId());

        //映射代理和商户名称
        result.getRecords().stream().forEach(stringObjectMap -> {
            String orgCode = (String) stringObjectMap.remove("orgCode");
            String orgName = DatabaseCache.getOrgNameByOrgcode(orgCode);
            stringObjectMap.put("orgName",orgName);

            Integer agentId = (Integer) stringObjectMap.remove("agentId");
            String agentName = DatabaseCache.getAgentNameByAgentId(agentId);
            stringObjectMap.put("agentName",agentName);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> statisticRateSuccessGroupBySendCode(SendRecordQuery sendRecord) {
        QueryWrapper queryWrapper = new QueryWrapper();
        //LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (sendRecord.getAgentId() != null){
            //queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
            queryWrapper.eq("r.agent_id",sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){
            //queryWrapper.eq(SendRecord::getOrgCode,sendRecord.getOrgCode());
            queryWrapper.eq("r.org_code",sendRecord.getOrgCode());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null ){//通道
            //queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
            queryWrapper.eq("r.channel_id",sendRecord.getChannelId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        /**
         * 2021-04-09
         */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince()) ){//号码归属地
            queryWrapper.eq("r.sms_number_province",sendRecord.getSmsNumberProvince());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            //queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
            queryWrapper.eq("r.send_code",sendRecord.getSendCode());
        }
        if(sendRecord.getSubTypeWith() != null) {
            queryWrapper.apply("r.sub_type&{0}=r.sub_type", sendRecord.getSubTypeWith());
        }
        queryWrapper.apply("r.`state`& 52= r.`state`");
        //queryWrapper.groupBy(BaseSend::getSendCode);
        queryWrapper.groupBy("r.send_code");
        //queryWrapper.orderByDesc(SendRecord::getCreateTime);
        queryWrapper.orderByDesc("r.create_time");
        if (sendRecord.getSubmitSuccessCount() != null){
            queryWrapper.having("total > {0}",sendRecord.getSubmitSuccessCount());
        }
        List<Map<String, Object>> result = this.reportMapper.statisticRateSuccessGroupBySendCode(queryWrapper,sendRecord.getGroupId());

        //映射代理和商户名称
        result.stream().forEach(stringObjectMap -> {
            String orgCode = (String) stringObjectMap.remove("orgCode");
            String orgName = DatabaseCache.getOrgNameByOrgcode(orgCode);
            stringObjectMap.put("orgName",orgName);

            Integer agentId = (Integer) stringObjectMap.remove("agentId");
            String agentName = DatabaseCache.getAgentNameByAgentId(agentId);
            stringObjectMap.put("agentName",agentName);
        });
        return result;
    }

    @Override
    public IPage<Map<String, Object>> statisticRateSuccessGroupByContent(QueryRequest request, SendRecordQuery sendRecord) {
        QueryWrapper queryWrapper = new QueryWrapper();

        //LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (sendRecord.getAgentId() != null){
            //queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
            queryWrapper.eq("r.agent_id",sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){
            //queryWrapper.eq(SendRecord::getOrgCode,sendRecord.getOrgCode());
            queryWrapper.eq("r.org_code",sendRecord.getOrgCode());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        /**
         * 2021-04-09
         */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince()) ){//号码归属地
            queryWrapper.eq("r.sms_number_province",sendRecord.getSmsNumberProvince());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            //queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
            queryWrapper.eq("r.send_code",sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//内容
            //queryWrapper.eq(BaseSend::getSmsContent,sendRecord.getSmsContent());
            queryWrapper.eq("r.sms_content",sendRecord.getSmsContent());
        }
        if(sendRecord.getSubTypeWith() != null) {
            queryWrapper.apply("r.sub_type&{0}=r.sub_type", sendRecord.getSubTypeWith());
        }

    	queryWrapper.apply("r.`state`& 52= r.`state`");
    	queryWrapper.groupBy("r.sms_content");
    	queryWrapper.orderByDesc("r.create_time");
    	Page<Map<String, Object>> page = new Page<>(request.getPageNum(), request.getPageSize());
    	IPage<Map<String, Object>> result = this.reportMapper.statisticRateSuccessGroupBySendCode(page, queryWrapper,sendRecord.getGroupId());

    	//映射代理和商户名称
    	result.getRecords().stream().forEach(stringObjectMap -> {
    		String orgCode = (String) stringObjectMap.remove("orgCode");
    		String orgName = DatabaseCache.getOrgNameByOrgcode(orgCode);
    		stringObjectMap.put("orgName",orgName);

    		Integer agentId = (Integer) stringObjectMap.remove("agentId");
    		String agentName = DatabaseCache.getAgentNameByAgentId(agentId);
    		stringObjectMap.put("agentName",agentName);
    	});
    	return result;
    }

    @Override
    public List<Map<String, Object>> statisticRateSuccessGroupByContent(SendRecordQuery sendRecord) {
        QueryWrapper queryWrapper = new QueryWrapper();

        //LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (sendRecord.getAgentId() != null){
            //queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
            queryWrapper.eq("r.agent_id",sendRecord.getAgentId());
        }
        if (StringUtils.isNotBlank(sendRecord.getOrgCode())){
            //queryWrapper.eq(SendRecord::getOrgCode,sendRecord.getOrgCode());
            queryWrapper.eq("r.org_code",sendRecord.getOrgCode());
        }
        if (sendRecord.getCreateStartTime() != null){//起始时间 for 创建时间
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//结束时间 for 创建时间
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//运营商
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//批次号
            //queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
            queryWrapper.eq("r.send_code",sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//内容
            //queryWrapper.eq(BaseSend::getSmsContent,sendRecord.getSmsContent());
            queryWrapper.eq("r.sms_content",sendRecord.getSmsContent());
        }
        if(sendRecord.getSubTypeWith() != null) {
            queryWrapper.apply("r.sub_type&{0}=r.sub_type", sendRecord.getSubTypeWith());
        }

        queryWrapper.apply("r.`state`& 52= r.`state`");
        queryWrapper.groupBy("r.sms_content");
        queryWrapper.orderByDesc("r.create_time");
        List<Map<String, Object>> result = this.reportMapper.statisticRateSuccessGroupBySendCode( queryWrapper,sendRecord.getGroupId());

        //映射代理和商户名称
        result.stream().forEach(stringObjectMap -> {
            String orgCode = (String) stringObjectMap.remove("orgCode");
            String orgName = DatabaseCache.getOrgNameByOrgcode(orgCode);
            stringObjectMap.put("orgName",orgName);

            Integer agentId = (Integer) stringObjectMap.remove("agentId");
            String agentName = DatabaseCache.getAgentNameByAgentId(agentId);
            stringObjectMap.put("agentName",agentName);
        });
        return result;
    }
}
