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
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormal;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormalQuery;
import com.hero.sms.mapper.message.ReceiptReturnRecordAbnormalMapper;
import com.hero.sms.service.message.IReceiptReturnRecordAbnormalService;

/**
 * 接收回执信息异常表 Service实现
 *
 * @author Administrator
 * @date 2020-11-21 17:50:39
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ReceiptReturnRecordAbnormalServiceImpl extends ServiceImpl<ReceiptReturnRecordAbnormalMapper, ReceiptReturnRecordAbnormal> implements IReceiptReturnRecordAbnormalService {

    @Autowired
    private ReceiptReturnRecordAbnormalMapper receiptReturnRecordAbnormalMapper;

    @Override
    public IPage<ReceiptReturnRecordAbnormal> findReceiptReturnRecordAbnormals(QueryRequest request, ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        LambdaQueryWrapper<ReceiptReturnRecordAbnormal> queryWrapper = new LambdaQueryWrapper<>();
        if(CollectionUtils.isNotEmpty(receiptReturnRecordAbnormal.getIds())) {
	    	queryWrapper.in(ReceiptReturnRecordAbnormal::getId, receiptReturnRecordAbnormal.getIds());
	    }
        if (receiptReturnRecordAbnormal.getId() != null){
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getId,receiptReturnRecordAbnormal.getId());
        }
        if (StringUtils.isNotBlank(receiptReturnRecordAbnormal.getSmsNumber())) {//手机号码
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getSmsNumber,receiptReturnRecordAbnormal.getSmsNumber());
        }
        if (receiptReturnRecordAbnormal.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(ReceiptReturnRecordAbnormal::getCreateTime,receiptReturnRecordAbnormal.getCreateStartTime());
        }
        if (receiptReturnRecordAbnormal.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(ReceiptReturnRecordAbnormal::getCreateTime,receiptReturnRecordAbnormal.getCreateEndTime());
        }
        if (receiptReturnRecordAbnormal.getChannelId() != null){//通道
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getChannelId,receiptReturnRecordAbnormal.getChannelId());
        }
        if (receiptReturnRecordAbnormal.getReturnState() != null){//接收状态
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getReturnState,receiptReturnRecordAbnormal.getReturnState());
        }
        if (receiptReturnRecordAbnormal.getProcessingState() != null){//处理状态
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getProcessingState,receiptReturnRecordAbnormal.getProcessingState());
        }
        if (StringUtils.isNotBlank(receiptReturnRecordAbnormal.getResMsgid())){//通道消息标识
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getResMsgid,receiptReturnRecordAbnormal.getResMsgid());
        }
	   	//创建时间的排序方式
	   	if(receiptReturnRecordAbnormal.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(receiptReturnRecordAbnormal.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(ReceiptReturnRecordAbnormal::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(ReceiptReturnRecordAbnormal::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(ReceiptReturnRecordAbnormal::getCreateTime);
	   	}
	   	
        // TODO 设置查询条件
        Page<ReceiptReturnRecordAbnormal> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<ReceiptReturnRecordAbnormal> findReceiptReturnRecordAbnormals(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
	    LambdaQueryWrapper<ReceiptReturnRecordAbnormal> queryWrapper = new LambdaQueryWrapper<>();
        if(CollectionUtils.isNotEmpty(receiptReturnRecordAbnormal.getIds())) {
	    	queryWrapper.in(ReceiptReturnRecordAbnormal::getId, receiptReturnRecordAbnormal.getIds());
	    }
        if (receiptReturnRecordAbnormal.getId() != null){
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getId,receiptReturnRecordAbnormal.getId());
        }
	    if (StringUtils.isNotBlank(receiptReturnRecordAbnormal.getSmsNumber())) {//手机号码
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getSmsNumber,receiptReturnRecordAbnormal.getSmsNumber());
        }
        if (receiptReturnRecordAbnormal.getCreateStartTime() != null){//起始时间 for 创建时间
            queryWrapper.ge(ReceiptReturnRecordAbnormal::getCreateTime,receiptReturnRecordAbnormal.getCreateStartTime());
        }
        if (receiptReturnRecordAbnormal.getCreateEndTime() != null){//结束时间 for 创建时间
            queryWrapper.le(ReceiptReturnRecordAbnormal::getCreateTime,receiptReturnRecordAbnormal.getCreateEndTime());
        }
        if (receiptReturnRecordAbnormal.getChannelId() != null){//通道
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getChannelId,receiptReturnRecordAbnormal.getChannelId());
        }
        if (receiptReturnRecordAbnormal.getReturnState() != null){//接收状态
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getReturnState,receiptReturnRecordAbnormal.getReturnState());
        }
        if (receiptReturnRecordAbnormal.getProcessingState() != null){//处理状态
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getProcessingState,receiptReturnRecordAbnormal.getProcessingState());
        }
        if (StringUtils.isNotBlank(receiptReturnRecordAbnormal.getResMsgid())){//通道消息标识
            queryWrapper.eq(ReceiptReturnRecordAbnormal::getResMsgid,receiptReturnRecordAbnormal.getResMsgid());
        }
	   	//创建时间的排序方式
	   	if(receiptReturnRecordAbnormal.getOrderByCreateTimeWay() != null)
	   	{
	   		if("ASC".equals(receiptReturnRecordAbnormal.getOrderByCreateTimeWay()))
	   		{
	   			queryWrapper.orderByAsc(ReceiptReturnRecordAbnormal::getCreateTime);
	   		}
	   		else
		   	{
		   		queryWrapper.orderByDesc(ReceiptReturnRecordAbnormal::getCreateTime);
		   	}
	   	}
	   	else
	   	{
	   		queryWrapper.orderByDesc(ReceiptReturnRecordAbnormal::getCreateTime);
	   	}
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        this.save(receiptReturnRecordAbnormal);
    }

    @Override
    @Transactional
    public void updateReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        this.saveOrUpdate(receiptReturnRecordAbnormal);
    }

    @Override
    @Transactional
    public void deleteReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        LambdaQueryWrapper<ReceiptReturnRecordAbnormal> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteReceiptReturnRecordAbnormals(String[] receiptReturnRecordAbnormalIds) {
        List<String> list = Arrays.asList(receiptReturnRecordAbnormalIds);
        this.removeByIds(list);
    }
}
