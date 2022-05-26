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
import com.hero.sms.entity.message.history.ReturnRecordHistory;
import com.hero.sms.entity.message.history.ReturnRecordHistoryQuery;
import com.hero.sms.mapper.message.ReturnRecordHistoryMapper;
import com.hero.sms.service.message.IReturnRecordHistoryService;

/**
 * 历史回执记录 Service实现
 *
 * @author Administrator
 * @date 2020-03-15 23:31:41
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ReturnRecordHistoryServiceImpl extends ServiceImpl<ReturnRecordHistoryMapper, ReturnRecordHistory> implements IReturnRecordHistoryService {

    @Autowired
    private ReturnRecordHistoryMapper returnRecordHistoryMapper;

    @Override
    public IPage<ReturnRecordHistory> findReturnRecordHistorys(QueryRequest request, ReturnRecordHistoryQuery returnRecordHistory) {
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
        queryWrapper.orderByDesc(ReturnRecordHistory::getCreateTime);
        Page<ReturnRecordHistory> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<ReturnRecordHistory> findReturnRecordHistorys(ReturnRecordHistoryQuery returnRecordHistory) {
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
        queryWrapper.orderByDesc(ReturnRecordHistory::getCreateTime);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createReturnRecordHistory(ReturnRecordHistory returnRecordHistory) {
        this.save(returnRecordHistory);
    }

    @Override
    @Transactional
    public void updateReturnRecordHistory(ReturnRecordHistoryQuery returnRecordHistory) {
        this.saveOrUpdate(returnRecordHistory);
    }

    @Override
    @Transactional
    public void deleteReturnRecordHistory(ReturnRecordHistoryQuery returnRecordHistory) {
        LambdaQueryWrapper<ReturnRecordHistory> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
        if (StringUtils.isNotBlank(returnRecordHistory.getSendCode())){//批次号
            wrapper.eq(ReturnRecordHistory::getSendCode,returnRecordHistory.getSendCode());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getOrgCode())){//商户编码
            wrapper.eq(ReturnRecordHistory::getOrgCode,returnRecordHistory.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getSmsNumber())){//手机号码
            wrapper.eq(ReturnRecordHistory::getSmsNumber,returnRecordHistory.getSmsNumber());
        }
        if (StringUtils.isNotBlank(returnRecordHistory.getResMsgid())){//通道消息标识
            wrapper.eq(ReturnRecordHistory::getResMsgid,returnRecordHistory.getResMsgid());
        }
        if (returnRecordHistory.getReqCreateStartTime() != null){//开始时间  for 提交时间
            wrapper.ge(ReturnRecordHistory::getReqCreateTime,returnRecordHistory.getReqCreateStartTime());
        }
        if (returnRecordHistory.getReqCreateEndTime() != null){//结束时间  for 提交时间
            wrapper.le(ReturnRecordHistory::getReqCreateTime,returnRecordHistory.getReqCreateEndTime());
        }
        if (returnRecordHistory.getChannelId() != null){//通道ID
            wrapper.eq(ReturnRecordHistory::getChannelId,returnRecordHistory.getChannelId());
        }
        if (returnRecordHistory.getSmsType() != null){//消息类型
            wrapper.eq(ReturnRecordHistory::getSmsType,returnRecordHistory.getSmsType());
        }
        if (returnRecordHistory.getReturnState() != null){//接收状态
            wrapper.eq(ReturnRecordHistory::getReturnState,returnRecordHistory.getReturnState());
        }
        if (returnRecordHistory.getReqState() != null){//请求状态
            wrapper.eq(ReturnRecordHistory::getReqState,returnRecordHistory.getReqState());
        }
        if (returnRecordHistory.getAgentId() != null){//代理商ID
            wrapper.eq(ReturnRecordHistory::getAgentId,returnRecordHistory.getAgentId());
        }
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteReturnRecordHistorys(String[] returnRecordHistoryIds) {
        List<String> list = Arrays.asList(returnRecordHistoryIds);
        this.removeByIds(list);
    }

    @Override
    public Integer countByQueryEntity(ReturnRecordHistoryQuery returnRecordHistory) {
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
        return count(queryWrapper);
    }
}
