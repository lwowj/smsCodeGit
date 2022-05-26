package com.hero.sms.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalChannel;
import com.hero.sms.entity.message.StatisticalChannelQuery;
import com.hero.sms.mapper.message.StatisticalChannelMapper;
import com.hero.sms.service.message.IStatisticalChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 按照通道统计 Service实现
 *
 * @author Administrator
 * @date 2020-05-28 15:59:07
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class StatisticalChannelServiceImpl extends ServiceImpl<StatisticalChannelMapper, StatisticalChannel> implements IStatisticalChannelService {

    @Autowired
    private StatisticalChannelMapper statisticalChannelMapper;

    @Override
    public IPage<StatisticalChannel> findStatisticalChannels(QueryRequest request, StatisticalChannelQuery statisticalChannel) {
        LambdaQueryWrapper<StatisticalChannel> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (statisticalChannel.getChannelId() != null){
            queryWrapper.eq(StatisticalChannel::getChannelId,statisticalChannel.getChannelId());
        }
        if (statisticalChannel.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalChannel::getStatisticalDate,statisticalChannel.getStatisticalStartTime());
        }
        if (statisticalChannel.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalChannel::getStatisticalDate,statisticalChannel.getStatisticalEndTime());
        }
        queryWrapper.orderByDesc(StatisticalChannel::getStatisticalDate);
        queryWrapper.orderByDesc(StatisticalChannel::getTotalCount);
        Page<StatisticalChannel> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<StatisticalChannel> findStatisticalChannels(StatisticalChannelQuery statisticalChannel) {
	    LambdaQueryWrapper<StatisticalChannel> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (statisticalChannel.getChannelId() != null){
            queryWrapper.eq(StatisticalChannel::getChannelId,statisticalChannel.getChannelId());
        }
        if (statisticalChannel.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalChannel::getStatisticalDate,statisticalChannel.getStatisticalStartTime());
        }
        if (statisticalChannel.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalChannel::getStatisticalDate,statisticalChannel.getStatisticalEndTime());
        }
        queryWrapper.orderByDesc(StatisticalChannel::getStatisticalDate);
        queryWrapper.orderByDesc(StatisticalChannel::getTotalCount);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createStatisticalChannel(StatisticalChannel statisticalChannel) {
        this.save(statisticalChannel);
    }

    @Override
    @Transactional
    public void updateStatisticalChannel(StatisticalChannel statisticalChannel) {
        this.saveOrUpdate(statisticalChannel);
    }

    @Override
    @Transactional
    public void deleteStatisticalChannel(StatisticalChannelQuery statisticalChannel) {
        LambdaQueryWrapper<StatisticalChannel> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteStatisticalChannels(String[] statisticalChannelIds) {
        List<String> list = Arrays.asList(statisticalChannelIds);
        this.removeByIds(list);
    }

    @Override
    public List<Map<String,Object>> sumStatisticalChannels(StatisticalChannelQuery statisticalChannel) {
        LambdaQueryWrapper<StatisticalChannel> queryWrapper = new LambdaQueryWrapper<>();
        if (statisticalChannel.getChannelId() != null){
            queryWrapper.eq(StatisticalChannel::getChannelId,statisticalChannel.getChannelId());
        }
        if (statisticalChannel.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalChannel::getStatisticalDate,statisticalChannel.getStatisticalStartTime());
        }
        if (statisticalChannel.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalChannel::getStatisticalDate,statisticalChannel.getStatisticalEndTime());
        }
        queryWrapper.groupBy(StatisticalChannel::getChannelId);
        return this.statisticalChannelMapper.sumStatisticalChannels(queryWrapper);
    }
}
