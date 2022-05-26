package com.hero.sms.service.impl.message;


import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoQuery;
import com.hero.sms.mapper.message.SendRecordCheckinfoMapper;
import com.hero.sms.service.message.ISendRecordCheckinfoService;

/**
 * 发送记录提交中间校验表 Service实现
 *
 * @author Administrator
 * @date 2020-12-21 17:46:28
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendRecordCheckinfoServiceImpl extends ServiceImpl<SendRecordCheckinfoMapper, SendRecordCheckinfo> implements ISendRecordCheckinfoService {

    @Autowired
    private SendRecordCheckinfoMapper sendRecordCheckinfoMapper;

    @Override
    public IPage<SendRecordCheckinfo> findSendRecordCheckinfos(QueryRequest request, SendRecordCheckinfo sendRecordCheckinfo) {
        LambdaQueryWrapper<SendRecordCheckinfo> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<SendRecordCheckinfo> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SendRecordCheckinfo> findSendRecordCheckinfos(SendRecordCheckinfo sendRecordCheckinfo) {
	    LambdaQueryWrapper<SendRecordCheckinfo> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSendRecordCheckinfo(SendRecordCheckinfo sendRecordCheckinfo) {
        this.save(sendRecordCheckinfo);
    }

    @Override
    @Transactional
    public void updateSendRecordCheckinfo(SendRecordCheckinfo sendRecordCheckinfo) {
        this.saveOrUpdate(sendRecordCheckinfo);
    }

    @Override
    @Transactional
    public void deleteSendRecordCheckinfo(SendRecordCheckinfo sendRecordCheckinfo) {
        LambdaQueryWrapper<SendRecordCheckinfo> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSendRecordCheckinfos(String[] sendRecordCheckinfoIds) {
        List<String> list = Arrays.asList(sendRecordCheckinfoIds);
        this.removeByIds(list);
    }
    
    @Override
    @Transactional
    public void updateSendRecordCheckinfoBatch(SendRecordCheckinfoQuery sendRecordCheckinfo) {
    	this.baseMapper.updateSendRecordCheckinfoBatch(sendRecordCheckinfo);
    }
}
