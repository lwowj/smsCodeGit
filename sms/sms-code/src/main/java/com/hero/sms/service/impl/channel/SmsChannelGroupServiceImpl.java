package com.hero.sms.service.impl.channel;

import com.google.common.collect.Lists;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.SmsChannelGroup;
import com.hero.sms.mapper.channel.SmsChannelGroupMapper;
import com.hero.sms.service.channel.ISmsChannelGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Arrays;

/**
 * 通道分组表 Service实现
 *
 * @author Administrator
 * @date 2020-06-20 22:38:31
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SmsChannelGroupServiceImpl extends ServiceImpl<SmsChannelGroupMapper, SmsChannelGroup> implements ISmsChannelGroupService {

    @Autowired
    private SmsChannelGroupMapper smsChannelGroupMapper;

    @Override
    public IPage<SmsChannelGroup> findSmsChannelGroups(QueryRequest request, SmsChannelGroup smsChannelGroup) {
        LambdaQueryWrapper<SmsChannelGroup> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<SmsChannelGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SmsChannelGroup> findSmsChannelGroups(SmsChannelGroup smsChannelGroup) {
	    LambdaQueryWrapper<SmsChannelGroup> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSmsChannelGroup(SmsChannelGroup smsChannelGroup) {
        this.save(smsChannelGroup);
    }

    @Override
    @Transactional
    public void updateSmsChannelGroup(SmsChannelGroup smsChannelGroup) {
        this.saveOrUpdate(smsChannelGroup);
    }

    @Override
    @Transactional
    public void deleteSmsChannelGroup(SmsChannelGroup smsChannelGroup) {
        LambdaQueryWrapper<SmsChannelGroup> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSmsChannelGroups(String[] smsChannelGroupIds) {
        List<String> list = Arrays.asList(smsChannelGroupIds);
        this.removeByIds(list);
    }

    @Override
    public void incrementSaveSmsChannelGroup(Integer smsChannelId, String groupIds) throws ServiceException {
        if (smsChannelId == null){
            throw new ServiceException("通道不能为空！");
        }
        //商户分组重置清空
        LambdaQueryWrapper<SmsChannelGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsChannelGroup::getSmsChannelId,smsChannelId);
        this.remove(wrapper);

        if (StringUtils.isBlank(groupIds)){
            return;
        }
        //商户分组重置保存
        List<SmsChannelGroup> list = Lists.newArrayList();
        Arrays.asList(groupIds.split(",")).stream().forEach( item -> {
            SmsChannelGroup group = new SmsChannelGroup();
            group.setSmsChannelId(smsChannelId);
            group.setGroupId(item);
            group.setCreateTime(new Date());
            list.add(group);
        });

        this.saveBatch(list);
    }
}
