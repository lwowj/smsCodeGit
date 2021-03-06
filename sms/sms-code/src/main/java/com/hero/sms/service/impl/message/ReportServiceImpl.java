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
            String provinceName = "??????";
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
            String operatorName = "??????";
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
        // TODO ??????????????????
        if(sendBox.getIsTiming() != null) {//????????????
            if(sendBox.getIsTiming())queryWrapper.isNotNull(SendBox::getTimingTime);
            else queryWrapper.isNull(SendBox::getTimingTime);
        }
        if(sendBox.getLeTimingTime() != null) {//????????????
            queryWrapper.le(SendBox::getTimingTime, sendBox.getLeTimingTime());
        }
        if (sendBox.getSortingStartTime() != null){//??????????????????
            queryWrapper.ge(SendBox::getSortingTime,sendBox.getSortingStartTime());
        }
        if (sendBox.getSortingEndTime() != null) {// ??????????????????
            queryWrapper.le(SendBox::getSortingTime, sendBox.getSortingEndTime());
        }
        if (StringUtils.isNotBlank(sendBox.getSendCode())) {// ?????????
            queryWrapper.eq(BaseSend::getSendCode, sendBox.getSendCode());
        }
        if (StringUtils.isNotBlank(sendBox.getCreateUsername())) {// ?????????
            queryWrapper.eq(SendBox::getCreateUsername, sendBox.getCreateUsername());
        }
        if (sendBox.getAgentId() != null) {// ?????????id
            queryWrapper.eq(BaseSend::getAgentId, sendBox.getAgentId());
        }
        if (sendBox.getUpAgentId() != null){//????????????
            queryWrapper.eq(SendBox::getUpAgentId,sendBox.getUpAgentId());
        }
        if (StringUtils.isNotBlank(sendBox.getOrgCode())) {// ????????????
            queryWrapper.eq(BaseSend::getOrgCode, sendBox.getOrgCode());
        }
        if (sendBox.getSmsType() != null) {// ????????????
            queryWrapper.eq(BaseSend::getSmsType, sendBox.getSmsType());
        }
        if (sendBox.getAuditState() != null) {// ????????????
            queryWrapper.eq(SendBox::getAuditState, sendBox.getAuditState());
        }
        if (sendBox.getSubType() != null) {// ????????????
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
        if(sendBox.getIsTimingFlag() != null) {//????????????(????????????)
	    	if(sendBox.getIsTimingFlag())queryWrapper.isNotNull(SendBox::getIsTimingTime);
	    	else queryWrapper.isNull(SendBox::getIsTimingTime);
	    }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendBox.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(BaseSend::getSmsNumberArea, sendBox.getSmsNumberArea());
		}
		/**
	     * ???????????????
	     * 1 ??????
	     * 2 ???
	     */
		if (sendBox.getIsLongSms()!= null){//???????????????
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
				//?????????????????????????????????70
				queryWrapper.le(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
			else
			{
				//??????70
				queryWrapper.gt(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
		}
		/**
		 * 2021-03-29
		 * ???????????????
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
        // TODO ??????????????????
        if(sendBoxHistory.getIsTiming() != null) {//????????????
            if(sendBoxHistory.getIsTiming())queryWrapper.isNotNull(SendBoxHistory::getTimingTime);
            else queryWrapper.isNull(SendBoxHistory::getTimingTime);
        }
        if(sendBoxHistory.getLeTimingTime() != null) {//????????????
            queryWrapper.ge(SendBoxHistory::getTimingTime, sendBoxHistory.getLeTimingTime());
        }
        if (sendBoxHistory.getSortingStartTime() != null){//??????????????????
            queryWrapper.ge(SendBoxHistory::getSortingTime,sendBoxHistory.getSortingStartTime());
        }
        if (sendBoxHistory.getSortingEndTime() != null) {// ??????????????????
            queryWrapper.le(SendBoxHistory::getSortingTime, sendBoxHistory.getSortingEndTime());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSendCode())) {// ?????????
            queryWrapper.eq(SendBoxHistory::getSendCode, sendBoxHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getCreateUsername())) {// ?????????
            queryWrapper.eq(SendBoxHistory::getCreateUsername, sendBoxHistory.getCreateUsername());
        }
        if (sendBoxHistory.getAgentId() != null) {// ?????????id
            queryWrapper.eq(SendBoxHistory::getAgentId, sendBoxHistory.getAgentId());
        }
        if (sendBoxHistory.getUpAgentId() != null){//????????????
            queryWrapper.eq(SendBoxHistory::getUpAgentId,sendBoxHistory.getUpAgentId());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getOrgCode())) {// ????????????
            queryWrapper.eq(SendBoxHistory::getOrgCode, sendBoxHistory.getOrgCode());
        }
        if (sendBoxHistory.getSmsType() != null) {// ????????????
            queryWrapper.eq(SendBoxHistory::getSmsType, sendBoxHistory.getSmsType());
        }
        if (sendBoxHistory.getAuditState() != null) {// ????????????
            queryWrapper.eq(SendBoxHistory::getAuditState, sendBoxHistory.getAuditState());
        }
        if (sendBoxHistory.getSubType() != null) {// ????????????
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
        if(sendBoxHistory.getIsTimingFlag() != null) {//????????????(????????????)
	    	if(sendBoxHistory.getIsTimingFlag())queryWrapper.isNotNull(SendBoxHistory::getIsTimingTime);
	    	else queryWrapper.isNull(SendBoxHistory::getIsTimingTime);
	    }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendBoxHistory.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(SendBoxHistory::getSmsNumberArea, sendBoxHistory.getSmsNumberArea());
		}
		/**
	     * ???????????????
	     * 1 ??????
	     * 2 ???
	     */
		if (sendBoxHistory.getIsLongSms()!= null){//???????????????
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
				//?????????????????????????????????70
				queryWrapper.le(SendBoxHistory::getSmsWords,wordsOfPerMsgInt);
			}
			else
			{
				//??????70
				queryWrapper.gt(SendBoxHistory::getSmsWords,wordsOfPerMsgInt);
			}
		}
        return this.reportMapper.statisticSendBoxHistoryInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticSendRecordInfo(SendRecordQuery sendRecord) {
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
        if (sendRecord.getState() != null){//??????
            queryWrapper.eq(SendRecord::getState,sendRecord.getState());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
        }
        if (sendRecord.getAgentId() != null){//?????????
            queryWrapper.eq(SendRecord::getAgentId,sendRecord.getAgentId());
        }
        if (sendRecord.getUpAgentId() != null){//???????????????
            queryWrapper.eq(SendRecord::getUpAgentId,sendRecord.getUpAgentId());
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
	   	 * @begin
	   	 * 2022-05-10
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
        return this.reportMapper.statisticSendRecordInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticSendRecordHistoryInfo(SendRecordHistoryQuery sendRecordHistory) {
        LambdaQueryWrapper<SendRecordHistory> queryWrapper = new LambdaQueryWrapper<>();
        // TODO ??????????????????
        if (StringUtils.isNotBlank(sendRecordHistory.getOrgCode())){//????????????
            queryWrapper.eq(SendRecordHistory::getOrgCode,sendRecordHistory.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSendCode())){//?????????
            queryWrapper.eq(SendRecordHistory::getSendCode,sendRecordHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumber())) {//????????????
            queryWrapper.eq(SendRecordHistory::getSmsNumber,sendRecordHistory.getSmsNumber());
        }
        if (sendRecordHistory.getCreateStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecordHistory::getCreateTime,sendRecordHistory.getCreateStartTime());
        }
        if (sendRecordHistory.getCreateEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecordHistory::getCreateTime,sendRecordHistory.getCreateEndTime());
        }
        /**
         * 2021-04-17
         * @begin
         */
        if (sendRecordHistory.getSubmitStartTime() != null){//???????????? for ????????????
            queryWrapper.ge(SendRecordHistory::getSubmitTime,sendRecordHistory.getSubmitStartTime());
        }
        if (sendRecordHistory.getSubmitEndTime() != null){//???????????? for ????????????
            queryWrapper.le(SendRecordHistory::getSubmitTime,sendRecordHistory.getSubmitEndTime());
        }
        /**
         * @end
         */
        if (sendRecordHistory.getChannelId() != null){//??????
            queryWrapper.eq(SendRecordHistory::getChannelId,sendRecordHistory.getChannelId());
        }
        if (sendRecordHistory.getState() != null){//??????
            queryWrapper.eq(SendRecordHistory::getState,sendRecordHistory.getState());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberOperator()) ){//?????????
            queryWrapper.eq(SendRecordHistory::getSmsNumberOperator,sendRecordHistory.getSmsNumberOperator());
        }
        if (sendRecordHistory.getAgentId() != null){
            queryWrapper.eq(SendRecordHistory::getAgentId,sendRecordHistory.getAgentId());
        }
        if (sendRecordHistory.getUpAgentId() != null){//???????????????
            queryWrapper.eq(SendRecordHistory::getUpAgentId,sendRecordHistory.getUpAgentId());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsContent())){//????????????
            queryWrapper.like(SendRecordHistory::getSmsContent,sendRecordHistory.getSmsContent());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberProvince())){//???????????????
            queryWrapper.eq(SendRecordHistory::getSmsNumberProvince,sendRecordHistory.getSmsNumberProvince());
        }
        if (sendRecordHistory.getSmsCount() != null){//????????????
            queryWrapper.eq(SendRecordHistory:: getSmsCount,sendRecordHistory.getSmsCount());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(SendRecordHistory::getSmsNumberArea, sendRecordHistory.getSmsNumberArea());
		}
        if (StringUtils.isNotBlank(sendRecordHistory.getResMsgid())){//????????????
            queryWrapper.eq(SendRecordHistory::getResMsgid,sendRecordHistory.getResMsgid());
        }
        return this.reportMapper.statisticSendRecordHistoryInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticReturnRecordInfo(ReturnRecordQuery returnRecord) {
        LambdaQueryWrapper<ReturnRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(returnRecord.getSendCode())){//?????????
            queryWrapper.eq(ReturnRecord::getSendCode,returnRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(returnRecord.getOrgCode())){//????????????
            queryWrapper.eq(ReturnRecord::getOrgCode,returnRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnRecord.getSmsNumber())){//????????????
            queryWrapper.eq(ReturnRecord::getSmsNumber,returnRecord.getSmsNumber());
        }
        if (StringUtils.isNotBlank(returnRecord.getResMsgid())){//??????????????????
            queryWrapper.eq(ReturnRecord::getResMsgid,returnRecord.getResMsgid());
        }
        if (returnRecord.getReqCreateStartTime() != null){//????????????  for ????????????
            queryWrapper.ge(ReturnRecord::getReqCreateTime,returnRecord.getReqCreateStartTime());
        }
        if (returnRecord.getReqCreateEndTime() != null){//????????????  for ????????????
            queryWrapper.le(ReturnRecord::getReqCreateTime,returnRecord.getReqCreateEndTime());
        }
        if (returnRecord.getChannelId() != null){//??????ID
            queryWrapper.eq(ReturnRecord::getChannelId,returnRecord.getChannelId());
        }
        if (returnRecord.getSmsType() != null){//????????????
            queryWrapper.eq(ReturnRecord::getSmsType,returnRecord.getSmsType());
        }
        if (returnRecord.getReturnState() != null){//????????????
            queryWrapper.eq(ReturnRecord::getReturnState,returnRecord.getReturnState());
        }
        if (returnRecord.getReqState() != null){//????????????
            queryWrapper.eq(ReturnRecord::getReqState,returnRecord.getReqState());
        }
        if (returnRecord.getAgentId() != null){//?????????ID
            queryWrapper.eq(ReturnRecord::getAgentId,returnRecord.getAgentId());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(returnRecord.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(ReturnRecord::getSmsNumberArea, returnRecord.getSmsNumberArea());
		}
        return this.reportMapper.statisticReturnRecordInfo(queryWrapper);
    }

    @Override
    public Map<String, Object> statisticReturnRecordHistoryInfo(ReturnRecordHistoryQuery returnRecordHistory) {
        LambdaQueryWrapper<ReturnRecordHistory> queryWrapper = new LambdaQueryWrapper<>();
        // TODO ??????????????????
        if (StringUtils.isNotBlank(returnRecordHistory.getSendCode())){//?????????
            queryWrapper.eq(ReturnRecordHistory::getSendCode,returnRecordHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getOrgCode())){//????????????
            queryWrapper.eq(ReturnRecordHistory::getOrgCode,returnRecordHistory.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getSmsNumber())){//????????????
            queryWrapper.eq(ReturnRecordHistory::getSmsNumber,returnRecordHistory.getSmsNumber());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getResMsgid())){//??????????????????
            queryWrapper.eq(ReturnRecordHistory::getResMsgid,returnRecordHistory.getResMsgid());
        }
        if (returnRecordHistory.getReqCreateStartTime() != null){//????????????  for ????????????
            queryWrapper.ge(ReturnRecordHistory::getReqCreateTime,returnRecordHistory.getReqCreateStartTime());
        }
        if (returnRecordHistory.getReqCreateEndTime() != null){//????????????  for ????????????
            queryWrapper.le(ReturnRecordHistory::getReqCreateTime,returnRecordHistory.getReqCreateEndTime());
        }
        if (returnRecordHistory.getChannelId() != null){//??????ID
            queryWrapper.eq(ReturnRecordHistory::getChannelId,returnRecordHistory.getChannelId());
        }
        if (returnRecordHistory.getSmsType() != null){//????????????
            queryWrapper.eq(ReturnRecordHistory::getSmsType,returnRecordHistory.getSmsType());
        }
        if (returnRecordHistory.getReturnState() != null){//????????????
            queryWrapper.eq(ReturnRecordHistory::getReturnState,returnRecordHistory.getReturnState());
        }
        if (returnRecordHistory.getReqState() != null){//????????????
            queryWrapper.eq(ReturnRecordHistory::getReqState,returnRecordHistory.getReqState());
        }
        if (returnRecordHistory.getAgentId() != null){//?????????ID
            queryWrapper.eq(ReturnRecordHistory::getAgentId,returnRecordHistory.getAgentId());
        }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(returnRecordHistory.getSmsNumberArea())) {// ????????????????????????
			queryWrapper.eq(ReturnRecordHistory::getSmsNumberArea, returnRecordHistory.getSmsNumberArea());
		}
        return this.reportMapper.statisticReturnRecordHistoryInfo(queryWrapper);
    }

    /**
     * ????????????????????????????????????
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
     * ????????????????????????????????????????????????
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
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null ){//??????
            //queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
            queryWrapper.eq("r.channel_id",sendRecord.getChannelId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        /**
         * 2021-04-09
         */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince()) ){//???????????????
            queryWrapper.eq("r.sms_number_province",sendRecord.getSmsNumberProvince());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
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

        //???????????????????????????
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
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (sendRecord.getChannelId() != null ){//??????
            //queryWrapper.eq(SendRecord::getChannelId,sendRecord.getChannelId());
            queryWrapper.eq("r.channel_id",sendRecord.getChannelId());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        /**
         * 2021-04-09
         */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince()) ){//???????????????
            queryWrapper.eq("r.sms_number_province",sendRecord.getSmsNumberProvince());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
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

        //???????????????????????????
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
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        /**
         * 2021-04-09
         */
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberProvince()) ){//???????????????
            queryWrapper.eq("r.sms_number_province",sendRecord.getSmsNumberProvince());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            //queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
            queryWrapper.eq("r.send_code",sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//??????
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

    	//???????????????????????????
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
        if (sendRecord.getCreateStartTime() != null){//???????????? for ????????????
            //queryWrapper.ge(SendRecord::getCreateTime,sendRecord.getCreateStartTime());
            queryWrapper.ge("r.create_time",sendRecord.getCreateStartTime());
        }
        if (sendRecord.getCreateEndTime() != null){//???????????? for ????????????
            //queryWrapper.le(SendRecord::getCreateTime,sendRecord.getCreateEndTime());
            queryWrapper.le("r.create_time",sendRecord.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsNumberOperator()) ){//?????????
            //queryWrapper.eq(SendRecord::getSmsNumberOperator,sendRecord.getSmsNumberOperator());
            queryWrapper.eq("r.sms_number_operator",sendRecord.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecord.getSendCode())){//?????????
            //queryWrapper.eq(BaseSend::getSendCode,sendRecord.getSendCode());
            queryWrapper.eq("r.send_code",sendRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecord.getSmsContent())){//??????
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

        //???????????????????????????
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
