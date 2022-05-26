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
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.mapper.channel.PayChannelMapper;
import com.hero.sms.service.channel.IPayChannelService;

/**
 * 支付通道 Service实现
 *
 * @author Administrator
 * @date 2020-03-12 11:02:02
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class PayChannelServiceImpl extends ServiceImpl<PayChannelMapper, PayChannel> implements IPayChannelService {

    @Autowired
    private PayChannelMapper payChannelMapper;
    @Autowired
    private DatabaseCache databaseCache;

    @Override
    public IPage<PayChannel> findPayChannels(QueryRequest request, PayChannel payChannel) {
        LambdaQueryWrapper<PayChannel> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(payChannel.getChannelName())){
            queryWrapper.like(PayChannel::getChannelName,payChannel.getChannelName());
        }
        if(StringUtils.isNotEmpty(payChannel.getMerchNo())){
            queryWrapper.eq(PayChannel::getMerchNo,payChannel.getMerchNo());
        }
        if(StringUtils.isNotEmpty(payChannel.getStateCode())){
            queryWrapper.eq(PayChannel::getStateCode,payChannel.getStateCode());
        }
        if(StringUtils.isNotEmpty(payChannel.getNetwayCode())){
            queryWrapper.eq(PayChannel::getNetwayCode,payChannel.getNetwayCode());
        }
        Page<PayChannel> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<PayChannel> findPayChannels(PayChannel payChannel) {
	    LambdaQueryWrapper<PayChannel> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createPayChannel(PayChannel payChannel) {
        if(payChannel == null || StringUtils.isEmpty(payChannel.getCallbackUrl())
                || StringUtils.isEmpty(payChannel.getChannelName()) || StringUtils.isEmpty(payChannel.getCost())
                || StringUtils.isEmpty(payChannel.getImplFullClass()) || StringUtils.isEmpty(payChannel.getMerchNo())
                || StringUtils.isEmpty(payChannel.getNetwayCode()) || StringUtils.isEmpty(payChannel.getPayCode())
                || StringUtils.isEmpty(payChannel.getRequestUrl()) || StringUtils.isEmpty(payChannel.getSignKey())
                || StringUtils.isEmpty(payChannel.getStateCode()) || payChannel.getMaxAmount() == null
                || payChannel.getWeight() == null || payChannel.getMinAmount() == null){
            throw new FebsException("通道信息不完整");
        }
        payChannel.setCreateTime(new Date());
        this.save(payChannel);
        // 加载下缓存
        databaseCache.init();
    }

    @Override
    @Transactional
    public void updatePayChannel(PayChannel payChannel) {
        if(payChannel == null || StringUtils.isEmpty(payChannel.getCallbackUrl())
                || StringUtils.isEmpty(payChannel.getChannelName()) || StringUtils.isEmpty(payChannel.getCost())
                || StringUtils.isEmpty(payChannel.getImplFullClass()) || StringUtils.isEmpty(payChannel.getMerchNo())
                || StringUtils.isEmpty(payChannel.getNetwayCode()) || StringUtils.isEmpty(payChannel.getPayCode())
                || StringUtils.isEmpty(payChannel.getRequestUrl()) || StringUtils.isEmpty(payChannel.getSignKey())
                || StringUtils.isEmpty(payChannel.getStateCode()) || payChannel.getMaxAmount() == null
                || payChannel.getWeight() == null || payChannel.getMinAmount() == null){
            throw new FebsException("通道信息不完整");
        }
        this.updateById(payChannel);
    }

    @Override
    @Transactional
    public void deletePayChannel(PayChannel payChannel) {
        LambdaQueryWrapper<PayChannel> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deletePayChannels(String[] payChannelIds) {
        List<String> list = Arrays.asList(payChannelIds);
        this.removeByIds(list);
        // 加载下缓存
        databaseCache.init();
    }
}
