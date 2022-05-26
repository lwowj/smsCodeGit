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
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.history.SendRecordHistory;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;
import com.hero.sms.mapper.message.SendRecordHistoryMapper;
import com.hero.sms.service.message.ISendRecordHistoryService;

/**
 * 历史发送记录 Service实现
 *
 * @author Administrator
 * @date 2020-03-15 23:31:38
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendRecordHistoryServiceImpl extends ServiceImpl<SendRecordHistoryMapper, SendRecordHistory> implements ISendRecordHistoryService {

    @Autowired
    private SendRecordHistoryMapper sendRecordHistoryMapper;

    @Override
    public IPage<SendRecordHistory> findSendRecordHistorys(QueryRequest request, SendRecordHistoryQuery sendRecordHistory) {
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
        if (sendRecordHistory.getAgentId() != null){//代理id
            queryWrapper.eq(SendRecordHistory::getAgentId,sendRecordHistory.getAgentId());
        }
        if (sendRecordHistory.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecordHistory::getUpAgentId,sendRecordHistory.getUpAgentId());
        }
        if (sendRecordHistory.getState() != null){//状态
            queryWrapper.eq(SendRecordHistory::getState,sendRecordHistory.getState());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecordHistory::getSmsNumberOperator,sendRecordHistory.getSmsNumberOperator());
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
        queryWrapper.orderByDesc(SendRecordHistory::getCreateTime);
        Page<SendRecordHistory> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SendRecordHistory> findSendRecordHistorys(SendRecordHistoryQuery sendRecordHistory) {
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
        if (sendRecordHistory.getAgentId() != null){//代理id
            queryWrapper.eq(SendRecordHistory::getAgentId,sendRecordHistory.getAgentId());
        }
        if (sendRecordHistory.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecordHistory::getUpAgentId,sendRecordHistory.getUpAgentId());
        }
        if (sendRecordHistory.getState() != null){//状态
            queryWrapper.eq(SendRecordHistory::getState,sendRecordHistory.getState());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecordHistory::getSmsNumberOperator,sendRecordHistory.getSmsNumberOperator());
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
        queryWrapper.orderByDesc(SendRecordHistory::getCreateTime);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSendRecordHistory(SendRecordHistory sendRecordHistory) {
        this.save(sendRecordHistory);
    }

    @Override
    @Transactional
    public void updateSendRecordHistory(SendRecordHistoryQuery sendRecordHistory) {
        this.saveOrUpdate(sendRecordHistory);
    }

    @Override
    @Transactional
    public void deleteSendRecordHistory(SendRecordHistoryQuery sendRecordHistory) {
        LambdaQueryWrapper<SendRecordHistory> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
        if (StringUtils.isNotBlank(sendRecordHistory.getOrgCode())){//商户编码
            wrapper.eq(SendRecordHistory::getOrgCode,sendRecordHistory.getOrgCode());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSendCode())){//批次号
            wrapper.eq(SendRecordHistory::getSendCode,sendRecordHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumber())) {//手机号码
            wrapper.eq(SendRecordHistory::getSmsNumber,sendRecordHistory.getSmsNumber());
        }
        if (sendRecordHistory.getCreateStartTime() != null){//起始时间 for 创建时间
            wrapper.ge(SendRecordHistory::getCreateTime,sendRecordHistory.getCreateStartTime());
        }
        if (sendRecordHistory.getCreateEndTime() != null){//结束时间 for 创建时间
            wrapper.le(SendRecordHistory::getCreateTime,sendRecordHistory.getCreateEndTime());
        }
        if (sendRecordHistory.getChannelId() != null){//通道
            wrapper.eq(SendRecordHistory::getChannelId,sendRecordHistory.getChannelId());
        }
        if (sendRecordHistory.getState() != null){//状态
            wrapper.eq(SendRecordHistory::getState,sendRecordHistory.getState());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberOperator()) ){//运营商
            wrapper.eq(SendRecordHistory::getSmsNumberOperator,sendRecordHistory.getSmsNumberOperator());
        }
        if (sendRecordHistory.getUpAgentId() != null){//上级代理id
            wrapper.eq(SendRecordHistory::getUpAgentId,sendRecordHistory.getUpAgentId());
        }
        if (sendRecordHistory.getAgentId() != null){
            wrapper.eq(SendRecordHistory::getAgentId,sendRecordHistory.getAgentId());
        }

	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSendRecordHistorys(String[] sendRecordHistoryIds) {
        List<String> list = Arrays.asList(sendRecordHistoryIds);
        this.removeByIds(list);
    }

    @Override
    public Integer countByQueryEntity(SendRecordHistoryQuery sendRecordHistory) {
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
        if (sendRecordHistory.getChannelId() != null){//通道
            queryWrapper.eq(SendRecordHistory::getChannelId,sendRecordHistory.getChannelId());
        }
        if (sendRecordHistory.getAgentId() != null){//代理id
            queryWrapper.eq(SendRecordHistory::getAgentId,sendRecordHistory.getAgentId());
        }
        if (sendRecordHistory.getUpAgentId() != null){//上级代理id
            queryWrapper.eq(SendRecordHistory::getUpAgentId,sendRecordHistory.getUpAgentId());
        }
        if (sendRecordHistory.getState() != null){//状态
            queryWrapper.eq(SendRecordHistory::getState,sendRecordHistory.getState());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsNumberOperator()) ){//运营商
            queryWrapper.eq(SendRecordHistory::getSmsNumberOperator,sendRecordHistory.getSmsNumberOperator());
        }
        if (StringUtils.isNotBlank(sendRecordHistory.getSmsContent())){
            queryWrapper.like(SendRecordHistory::getSmsContent,sendRecordHistory.getSmsContent());
        }
        return count(queryWrapper);
    }
}
