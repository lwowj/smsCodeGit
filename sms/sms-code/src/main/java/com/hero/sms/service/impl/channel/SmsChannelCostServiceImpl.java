package com.hero.sms.service.impl.channel;

import java.util.Arrays;
import java.util.Date;
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
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelCost;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.mapper.channel.SmsChannelCostMapper;
import com.hero.sms.service.channel.ISmsChannelCostService;
import com.hero.sms.utils.StringUtil;

/**
 * 通道资费 Service实现
 *
 * @author Administrator
 * @date 2020-03-10 15:27:23
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SmsChannelCostServiceImpl extends ServiceImpl<SmsChannelCostMapper, SmsChannelCost> implements ISmsChannelCostService {

    @Autowired
    private SmsChannelCostMapper smsChannelCostMapper;

    @Override
    public IPage<SmsChannelCost> findSmsChannelCosts(QueryRequest request, SmsChannelCost smsChannelCost) {
        LambdaQueryWrapper<SmsChannelCost> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (smsChannelCost.getSmsChannelId() != null){
            queryWrapper.eq(SmsChannelCost::getSmsChannelId,smsChannelCost.getSmsChannelId());
        }
        if (smsChannelCost.getSmsType() != null){
            queryWrapper.eq(SmsChannelCost::getSmsType,smsChannelCost.getSmsType());
        }
        if (StringUtils.isNotBlank(smsChannelCost.getName())){
            queryWrapper.eq(SmsChannelCost::getName,smsChannelCost.getName());
        }
        Page<SmsChannelCost> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SmsChannelCost> findSmsChannelCosts(SmsChannelCost smsChannelCost) {
	    LambdaQueryWrapper<SmsChannelCost> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
	    if(smsChannelCost.getSmsChannelId() != null) {
	    	queryWrapper.eq(SmsChannelCost::getSmsChannelId, smsChannelCost.getSmsChannelId());
	    }
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSmsChannelCost(SmsChannelCost smsChannelCost) throws ServiceException {
        //必填项判断
        if (smsChannelCost.getSmsChannelId() == null){
            throw new ServiceException("资费通道不能为空！");
        }
        if (smsChannelCost.getSmsType() == null){
            throw new ServiceException("短信类型不能为空");
        }
        if (smsChannelCost.getName() == null){
            throw new ServiceException("资费名称不能为空");
        }
        if (smsChannelCost.getValue() == null){
            throw new ServiceException("资费不能为空");
        }
        //检查是否存在同类唯一的资费记录
        LambdaQueryWrapper<SmsChannelCost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsChannelCost::getSmsType,smsChannelCost.getSmsType())
                .eq(SmsChannelCost::getSmsChannelId,smsChannelCost.getSmsChannelId())
                .eq(SmsChannelCost::getName,smsChannelCost.getName());
        //新增运营商字段
        if(!smsChannelCost.getName().equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	smsChannelCost.setOperator(null);
        }
        else
        {
        	if(StringUtil.isNotBlank(smsChannelCost.getOperator()))
        	{
        		queryWrapper.eq(SmsChannelCost::getOperator, smsChannelCost.getOperator());
        	}
        	else
        	{
        		queryWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(SmsChannelCost::getOperator)
    	    			.or().eq(SmsChannelCost::getOperator, "")
    	    			);
        	}
        }
        int existCount = count(queryWrapper);
        if (existCount > 0){
            throw new ServiceException("资费已经存在,请勿重新添加");
        }
        smsChannelCost.setCreateTime(new Date());
        this.save(smsChannelCost);
    }

    @Override
    @Transactional
    public void updateSmsChannelCost(SmsChannelCost smsChannelCost) throws ServiceException 
    {
    	//检查是否存在同类唯一的资费记录
        LambdaQueryWrapper<SmsChannelCost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsChannelCost::getSmsType,smsChannelCost.getSmsType())
                .eq(SmsChannelCost::getSmsChannelId,smsChannelCost.getSmsChannelId())
                .eq(SmsChannelCost::getName,smsChannelCost.getName())
                .ne(SmsChannelCost::getId, smsChannelCost.getId());
        //新增运营商字段
        if(!smsChannelCost.getName().equals(SmsNumberAreaCodeEnums.China.getInArea()))
        {
        	smsChannelCost.setOperator(null);
        }
        else
        {
        	if(StringUtil.isNotBlank(smsChannelCost.getOperator()))
        	{
        		queryWrapper.eq(SmsChannelCost::getOperator, smsChannelCost.getOperator());
        	}
        	else
        	{
        		queryWrapper.and(
    	    			wrapper -> 
    	    			wrapper.isNull(SmsChannelCost::getOperator)
    	    			.or().eq(SmsChannelCost::getOperator, "")
    	    			);
        	}
        }
        int existCount = count(queryWrapper);
        if (existCount > 0){
            throw new ServiceException("修改后的资费信息类型已经存在,请重新修改");
        }
        this.saveOrUpdate(smsChannelCost);
    }

    @Override
    @Transactional
    public void deleteSmsChannelCost(SmsChannelCost smsChannelCost) {
        LambdaQueryWrapper<SmsChannelCost> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSmsChannelCosts(String[] smsChannelCostIds) {
        List<String> list = Arrays.asList(smsChannelCostIds);
        this.removeByIds(list);
    }

    @Override
    public List<SmsChannelCost> queryListByChannelId(Integer id) {
        LambdaQueryWrapper<SmsChannelCost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsChannelCost::getSmsChannelId,id);
        List<SmsChannelCost> list = list(wrapper);
        if (list == null || list.size() == 0){

        }
        return list;
    }
}
