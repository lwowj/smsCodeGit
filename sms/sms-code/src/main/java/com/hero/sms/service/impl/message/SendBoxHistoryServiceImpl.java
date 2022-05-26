package com.hero.sms.service.impl.message;

import java.util.Arrays;
import java.util.List;

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
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.mapper.message.SendBoxHistoryMapper;
import com.hero.sms.service.message.ISendBoxHistoryService;

/**
 * 历史发件箱 Service实现
 *
 * @author Administrator
 * @date 2020-03-15 23:31:27
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendBoxHistoryServiceImpl extends ServiceImpl<SendBoxHistoryMapper, SendBoxHistory> implements ISendBoxHistoryService {

    @Autowired
    private SendBoxHistoryMapper sendBoxHistoryMapper;

    @Override
    public IPage<SendBoxHistory> findSendBoxHistorys(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {
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
        if (sendBoxHistory.getUpAgentId() != null){//上级代理id
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
        //过滤掉手机号码字段
        queryWrapper.select(SendBoxHistory.class, tableFieldInfo -> !tableFieldInfo.getColumn().equals("sms_numbers"));
        queryWrapper.orderByDesc(SendBoxHistory::getCreateTime);
        Page<SendBoxHistory> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }


    @Override
    public String getSmsNumbersByID(Long id){
        LambdaQueryWrapper<SendBoxHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SendBoxHistory::getSmsNumbers,SendBoxHistory::getType)
                .eq(SendBoxHistory::getId,id);
        SendBoxHistory sendBoxHistory = this.getOne(queryWrapper);
        //无记录或excel提交的   返回null
        if (sendBoxHistory == null || sendBoxHistory.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()){
            return null;
        }
        return sendBoxHistory.getSmsNumbers();
    }

    @Override
    public List<SendBoxHistory> findSendBoxHistorys(SendBoxHistoryQuery sendBoxHistory) {
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
        if (sendBoxHistory.getUpAgentId() != null){//上级代理id
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
		
        queryWrapper.orderByDesc(SendBoxHistory::getCreateTime);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSendBoxHistory(SendBoxHistory sendBoxHistory) {
        this.save(sendBoxHistory);
    }

    @Override
    @Transactional
    public void updateSendBoxHistory(SendBoxHistoryQuery sendBoxHistory) {
        this.saveOrUpdate(sendBoxHistory);
    }

    @Override
    @Transactional
    public void deleteSendBoxHistory(SendBoxHistoryQuery sendBoxHistory) {
        LambdaQueryWrapper<SendBoxHistory> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
        if(sendBoxHistory.getIsTiming() != null) {//是否定时
            if(sendBoxHistory.getIsTiming())wrapper.isNotNull(SendBoxHistory::getTimingTime);
            else wrapper.isNull(SendBoxHistory::getTimingTime);
        }
        if(sendBoxHistory.getLeTimingTime() != null) {//定时时间
            wrapper.ge(SendBoxHistory::getTimingTime, sendBoxHistory.getLeTimingTime());
        }
        if (sendBoxHistory.getSortingStartTime() != null){//分拣开始时间
            wrapper.ge(SendBoxHistory::getSortingTime,sendBoxHistory.getSortingStartTime());
        }
        if (sendBoxHistory.getSortingEndTime() != null) {// 分拣结束时间
            wrapper.le(SendBoxHistory::getSortingTime, sendBoxHistory.getSortingEndTime());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSendCode())) {// 批次号
            wrapper.eq(SendBoxHistory::getSendCode, sendBoxHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getCreateUsername())) {// 提交人
            wrapper.eq(SendBoxHistory::getCreateUsername, sendBoxHistory.getCreateUsername());
        }
        if (sendBoxHistory.getAgentId() != null) {// 代理商id
            wrapper.eq(SendBoxHistory::getAgentId, sendBoxHistory.getAgentId());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getOrgCode())) {// 商户编码
            wrapper.eq(SendBoxHistory::getOrgCode, sendBoxHistory.getOrgCode());
        }
        if (sendBoxHistory.getSmsType() != null) {// 短信类型
            wrapper.eq(SendBoxHistory::getSmsType, sendBoxHistory.getSmsType());
        }
        if (sendBoxHistory.getAuditState() != null) {// 审核状态
            wrapper.eq(SendBoxHistory::getAuditState, sendBoxHistory.getAuditState());
        }
        if (sendBoxHistory.getSubType() != null) {// 提交方式
            wrapper.eq(SendBoxHistory::getSubType, sendBoxHistory.getSubType());
        }
        if (sendBoxHistory.getCreateStartTime() != null){
            wrapper.ge(SendBoxHistory::getCreateTime,sendBoxHistory.getCreateStartTime());
        }
        if (sendBoxHistory.getCreateEndTime() != null){
            wrapper.le(SendBoxHistory::getCreateTime,sendBoxHistory.getCreateEndTime());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSmsNumbers())){
            wrapper.like(SendBoxHistory::getSmsNumbers,sendBoxHistory.getSmsNumbers());
        }
        if (StringUtils.isNotBlank(sendBoxHistory.getSmsContent())){
            wrapper.like(SendBoxHistory::getSmsContent,sendBoxHistory.getSmsContent());
        }
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSendBoxHistorys(String[] sendBoxHistoryIds) {
        List<String> list = Arrays.asList(sendBoxHistoryIds);
        this.removeByIds(list);
    }

    @Override
    public int countByEntity(SendBoxHistoryQuery sendBoxHistory) {
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
        if (sendBoxHistory.getUpAgentId() != null){//上级代理id
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
        return count(queryWrapper);
    }
}
