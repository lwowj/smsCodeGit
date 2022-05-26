package com.hero.sms.service.impl.message;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.commands.utils.ChainUtil;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.ReturnRecordQuery;
import com.hero.sms.mapper.message.ReturnRecordMapper;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.message.IReceiptReturnRecordAbnormalService;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.service.organization.IOrganizationService;

/**
 * 回执记录 Service实现
 *
 * @author Administrator
 * @date 2020-03-12 00:40:26
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ReturnRecordServiceImpl extends ServiceImpl<ReturnRecordMapper, ReturnRecord> implements IReturnRecordService {

    @Autowired
    private ReturnRecordMapper returnRecordMapper;

	@Autowired
	private ISmsChannelService smsChannelService;

	@Lazy
	@Autowired
	private ISendRecordService sendRecordService;
	
	@Lazy
	@Autowired
	private IReceiptReturnRecordAbnormalService receiptReturnRecordAbnormalService;
	
	@Autowired
	private IOrganizationService organizationService;

    @Override
    public IPage<ReturnRecord> findReturnRecords(QueryRequest request, ReturnRecordQuery returnRecord) {
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
        if (returnRecord.getCreateStartTime() != null){//开始时间  for 创建时间
            queryWrapper.ge(ReturnRecord::getCreateTime,returnRecord.getCreateStartTime());
        }
        if (returnRecord.getCreateEndTime() != null){//结束时间  for 创建时间
            queryWrapper.le(ReturnRecord::getCreateTime,returnRecord.getCreateEndTime());
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
        queryWrapper.orderByDesc(ReturnRecord::getCreateTime);
        // TODO 设置查询条件
        Page<ReturnRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<ReturnRecord> findReturnRecords(ReturnRecordQuery returnRecord) {
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
        if (returnRecord.getCreateStartTime() != null){//开始时间  for 创建时间
            queryWrapper.ge(ReturnRecord::getCreateTime,returnRecord.getCreateStartTime());
        }
        if (returnRecord.getCreateEndTime() != null){//结束时间  for 创建时间
            queryWrapper.le(ReturnRecord::getCreateTime,returnRecord.getCreateEndTime());
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
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createReturnRecord(ReturnRecord returnRecord) {
        this.save(returnRecord);
    }

    @Override
    @Transactional
    public void updateReturnRecord(ReturnRecord returnRecord) {
        this.saveOrUpdate(returnRecord);
    }

    @Override
    @Transactional
    public void deleteReturnRecord(ReturnRecordQuery returnRecord) {
        LambdaQueryWrapper<ReturnRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(returnRecord.getSendCode())){//批次号
            wrapper.eq(ReturnRecord::getSendCode,returnRecord.getSendCode());
        }
        if (StringUtils.isNotBlank(returnRecord.getOrgCode())){//商户编码
            wrapper.eq(ReturnRecord::getOrgCode,returnRecord.getOrgCode());
        }
        if (StringUtils.isNotBlank(returnRecord.getSmsNumber())){//手机号码
            wrapper.eq(ReturnRecord::getSmsNumber,returnRecord.getSmsNumber());
        }
        if (StringUtils.isNotBlank(returnRecord.getResMsgid())){//通道消息标识
            wrapper.eq(ReturnRecord::getResMsgid,returnRecord.getResMsgid());
        }
        if (returnRecord.getReqCreateStartTime() != null){//开始时间  for 提交时间
            wrapper.ge(ReturnRecord::getReqCreateTime,returnRecord.getReqCreateStartTime());
        }
        if (returnRecord.getReqCreateEndTime() != null){//结束时间  for 提交时间
            wrapper.le(ReturnRecord::getReqCreateTime,returnRecord.getReqCreateEndTime());
        }
        if (returnRecord.getCreateStartTime() != null){//开始时间  for 创建时间
            wrapper.ge(ReturnRecord::getCreateTime,returnRecord.getCreateStartTime());
        }
        if (returnRecord.getCreateEndTime() != null){//结束时间  for 创建时间
            wrapper.le(ReturnRecord::getCreateTime,returnRecord.getCreateEndTime());
        }
        if (returnRecord.getChannelId() != null){//通道ID
            wrapper.eq(ReturnRecord::getChannelId,returnRecord.getChannelId());
        }
        if (returnRecord.getSmsType() != null){//消息类型
            wrapper.eq(ReturnRecord::getSmsType,returnRecord.getSmsType());
        }
        if (returnRecord.getReturnState() != null){//接收状态
            wrapper.eq(ReturnRecord::getReturnState,returnRecord.getReturnState());
        }
        if (returnRecord.getReqState() != null){//请求状态
            wrapper.eq(ReturnRecord::getReqState,returnRecord.getReqState());
        }
        if (returnRecord.getAgentId() != null){//代理商ID
            wrapper.eq(ReturnRecord::getAgentId,returnRecord.getAgentId());
        }
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteReturnRecords(String[] returnRecordIds) {
        List<String> list = Arrays.asList(returnRecordIds);
        this.removeByIds(list);
    }

	@Override
	@Transactional(rollbackFor = { RuntimeException.class, Error.class })
	public void receiptReturnRecord(String channelCode, String resultData) {
		try {
			Command cmd = ChainUtil.getChain("receiptReturnRecord");
			ContextBase context = new ContextBase();
			context.put(BaseCommand.OBJ_ORG_SERVICE, organizationService);
			context.put(BaseCommand.OBJ_CHANNEL_SERVICE, smsChannelService);
			context.put(BaseCommand.OBJ_SENDRECORD_SERVICE, sendRecordService);
			context.put(BaseCommand.OBJ_RETURN_RECORD_SERVICE, this);
			context.put(BaseCommand.STR_CHANNEL_CODE, channelCode);
			context.put(BaseCommand.STR_RESULT_DATA, resultData);
			if(cmd.execute(context)) {
				log.error(context.get(BaseCommand.STR_ERROR_INFO).toString());
			}
		} catch (Exception e) {
			log.error(String.format("通道【%s】回执数据【%s】处理失败", channelCode,resultData), e);
		}
	}

	@Override
	public void receiptReturnRecord(ReturnRecord returnRecord,String msgId) {
		try {
			//4-1-1-1 根据chain-cfg.xml中的配置receiptReturnRecord1，进行相关校验、业务逻辑处理
			//最后在NotifyReturnStateCommand中进行将短信结果通知下游商户的操作
            //1、获取上游通道 QuerySmsChannelCommand
            //2、保存回执信息 SaveReturnRecordFromGateCommand
            //3、封装我们格式进行通知推送到MQ  NotifyReturnStateCommand
			Command cmd = ChainUtil.getChain("receiptReturnRecord1");
			ContextBase context = new ContextBase();
			context.put(BaseCommand.INT_CHANNEL_ID, returnRecord.getChannelId());
			context.put(BaseCommand.OBJ_RETURN_RECORD, returnRecord);
			context.put(BaseCommand.OBJ_ORG_SERVICE, organizationService);
			context.put(BaseCommand.OBJ_CHANNEL_SERVICE, smsChannelService);
			context.put(BaseCommand.OBJ_SENDRECORD_SERVICE, sendRecordService);
			context.put(BaseCommand.OBJ_RECEIPT_RETURN_RECORD_SERVICE, receiptReturnRecordAbnormalService);
			context.put(BaseCommand.OBJ_RETURN_RECORD_SERVICE, this);
			if(cmd.execute(context)) {
				log.error(context.get(BaseCommand.STR_ERROR_INFO).toString());
			}
		} catch (Exception e) {
			log.error(String.format("【%s】通道【%s】回执数据【%s】处理失败", msgId,returnRecord.getChannelId(),JSON.toJSONString(returnRecord)), e);
		}
	}

    @Override
    public Integer countByQueryEntity(ReturnRecordQuery returnRecord) {
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
        if (returnRecord.getCreateStartTime() != null){//开始时间  for 创建时间
            queryWrapper.ge(ReturnRecord::getCreateTime,returnRecord.getCreateStartTime());
        }
        if (returnRecord.getCreateEndTime() != null){//结束时间  for 创建时间
            queryWrapper.le(ReturnRecord::getCreateTime,returnRecord.getCreateEndTime());
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
        return count(queryWrapper);
    }
}
