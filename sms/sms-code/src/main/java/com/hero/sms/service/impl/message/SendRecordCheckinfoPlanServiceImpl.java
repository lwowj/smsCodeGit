package com.hero.sms.service.impl.message;


import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoPlan;
import com.hero.sms.entity.message.SendRecordCheckinfoPlanQuery;
import com.hero.sms.mapper.message.SendRecordCheckinfoPlanMapper;
import com.hero.sms.service.message.ISendRecordCheckinfoPlanService;

/**
 * 发送记录提交中间校验表 Service实现
 *
 * @author Administrator
 * @date 2022-05-02 20:49:18
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendRecordCheckinfoPlanServiceImpl extends ServiceImpl<SendRecordCheckinfoPlanMapper, SendRecordCheckinfoPlan> implements ISendRecordCheckinfoPlanService {

    @Autowired
    private SendRecordCheckinfoPlanMapper sendRecordCheckinfoPlanMapper;

    @Override
    public IPage<SendRecordCheckinfoPlan> findSendRecordCheckinfoPlans(QueryRequest request, SendRecordCheckinfoPlan sendRecordCheckinfoPlan) {
        LambdaQueryWrapper<SendRecordCheckinfoPlan> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<SendRecordCheckinfoPlan> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SendRecordCheckinfoPlan> findSendRecordCheckinfoPlans(SendRecordCheckinfoPlan sendRecordCheckinfoPlan) {
	    LambdaQueryWrapper<SendRecordCheckinfoPlan> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSendRecordCheckinfoPlan(SendRecordCheckinfoPlan sendRecordCheckinfoPlan) {
        this.save(sendRecordCheckinfoPlan);
    }

    @Override
    @Transactional
    public void updateSendRecordCheckinfoPlan(SendRecordCheckinfoPlan sendRecordCheckinfoPlan) {
        this.saveOrUpdate(sendRecordCheckinfoPlan);
    }

    @Override
    @Transactional
    public void deleteSendRecordCheckinfoPlan(SendRecordCheckinfoPlan sendRecordCheckinfoPlan) {
        LambdaQueryWrapper<SendRecordCheckinfoPlan> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSendRecordCheckinfoPlans(String[] sendRecordCheckinfoPlanIds) {
        List<String> list = Arrays.asList(sendRecordCheckinfoPlanIds);
        this.removeByIds(list);
    }
    
    @Override
    @Transactional
    public void updateSendRecordCheckinfoPlanBatch(SendRecordCheckinfoPlanQuery sendRecordCheckinfoPlan) {
    	LambdaUpdateWrapper<SendRecordCheckinfoPlan> updateSendRecordCheckinfoPlanWrapper = new LambdaUpdateWrapper<>();
    	updateSendRecordCheckinfoPlanWrapper.in(SendRecordCheckinfoPlan::getSendRecordId,sendRecordCheckinfoPlan.getIdsList());
    	updateSendRecordCheckinfoPlanWrapper.eq(SendRecordCheckinfoPlan::getState,sendRecordCheckinfoPlan.getState());//锁定状态1
    	updateSendRecordCheckinfoPlanWrapper.set(SendRecordCheckinfoPlan::getState,sendRecordCheckinfoPlan.getStates());//修改为解锁状态
 		this.update(updateSendRecordCheckinfoPlanWrapper);
    }
}
