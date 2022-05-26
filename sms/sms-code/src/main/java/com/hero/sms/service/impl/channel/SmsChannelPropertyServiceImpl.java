package com.hero.sms.service.impl.channel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.mapper.channel.SmsChannelPropertyMapper;
import com.hero.sms.service.channel.ISmsChannelPropertyService;

/**
 * 短信通道属性 Service实现
 *
 * @author Administrator
 * @date 2020-03-08 17:35:16
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SmsChannelPropertyServiceImpl extends ServiceImpl<SmsChannelPropertyMapper, SmsChannelProperty> implements ISmsChannelPropertyService {

    @Autowired
    private SmsChannelPropertyMapper smsChannelPropertyMapper;

    @Override
    public IPage<SmsChannelProperty> findSmsChannelPropertys(QueryRequest request, SmsChannelProperty smsChannelProperty) {
        LambdaQueryWrapper<SmsChannelProperty> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<SmsChannelProperty> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SmsChannelProperty> findSmsChannelPropertys(SmsChannelProperty smsChannelProperty) {
	    LambdaQueryWrapper<SmsChannelProperty> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSmsChannelProperty(SmsChannelProperty smsChannelProperty){
        smsChannelProperty.setCreateTime(new Date());
        this.save(smsChannelProperty);
    }

    @Override
    @Transactional
    public void updateSmsChannelProperty(SmsChannelProperty smsChannelProperty) {
        this.saveOrUpdate(smsChannelProperty);
    }

    @Override
    @Transactional
    public void deleteSmsChannelProperty(SmsChannelProperty smsChannelProperty) {
        LambdaQueryWrapper<SmsChannelProperty> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSmsChannelPropertys(String[] smsChannelPropertyIds) {
        List<String> list = Arrays.asList(smsChannelPropertyIds);
        this.removeByIds(list);
    }

    @Override
    public List<SmsChannelProperty> queryListByChannelId(Integer id) {
        LambdaQueryWrapper<SmsChannelProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsChannelProperty::getSmsChannelId,id);
        return list(wrapper);
    }
    
    @Override
    public List<SmsChannelProperty> queryListByChannelCode(String channelCode) {
    	return smsChannelPropertyMapper.selectByChannelCode(channelCode);
    }

    @Override
    public void updateSmsChannelProperties(List<SmsChannelProperty> smsChannelProperties, String username) {
        if (smsChannelProperties.size() > 0){
            LambdaQueryWrapper<SmsChannelProperty> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(SmsChannelProperty::getSmsChannelId,smsChannelProperties.get(0).getSmsChannelId());
            this.remove(deleteWrapper);
            List<SmsChannelProperty> saveList = smsChannelProperties.stream()
                    .filter(smsChannelProperty -> {
                        boolean result = StringUtils.isNotBlank(smsChannelProperty.getName());
                        if (result){
                            smsChannelProperty.setDescription("");
                            smsChannelProperty.setRemark(username + "添加");
                            smsChannelProperty.setCreateTime(new Date());
                        }
                        return result;
                    }).collect(Collectors.toList());

            this.saveBatch(saveList);
        }
    }
}
