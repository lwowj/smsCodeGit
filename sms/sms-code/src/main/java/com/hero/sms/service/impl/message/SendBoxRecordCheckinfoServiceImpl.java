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
import com.hero.sms.entity.message.SendBoxRecordCheckinfo;
import com.hero.sms.mapper.message.SendBoxRecordCheckinfoMapper;
import com.hero.sms.service.message.ISendBoxRecordCheckinfoService;

/**
 * 发件箱分拣发送记录提交中间校验表 Service实现
 *
 * @author Administrator
 * @date 2021-01-13 18:01:35
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendBoxRecordCheckinfoServiceImpl extends ServiceImpl<SendBoxRecordCheckinfoMapper, SendBoxRecordCheckinfo> implements ISendBoxRecordCheckinfoService {

    @Autowired
    private SendBoxRecordCheckinfoMapper sendBoxRecordCheckinfoMapper;

    @Override
    public IPage<SendBoxRecordCheckinfo> findSendBoxRecordCheckinfos(QueryRequest request, SendBoxRecordCheckinfo sendBoxRecordCheckinfo) {
        LambdaQueryWrapper<SendBoxRecordCheckinfo> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<SendBoxRecordCheckinfo> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SendBoxRecordCheckinfo> findSendBoxRecordCheckinfos(SendBoxRecordCheckinfo sendBoxRecordCheckinfo) {
	    LambdaQueryWrapper<SendBoxRecordCheckinfo> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSendBoxRecordCheckinfo(SendBoxRecordCheckinfo sendBoxRecordCheckinfo) {
        this.save(sendBoxRecordCheckinfo);
    }

    @Override
    @Transactional
    public void updateSendBoxRecordCheckinfo(SendBoxRecordCheckinfo sendBoxRecordCheckinfo) {
        this.saveOrUpdate(sendBoxRecordCheckinfo);
    }

    @Override
    @Transactional
    public void deleteSendBoxRecordCheckinfo(SendBoxRecordCheckinfo sendBoxRecordCheckinfo) {
        LambdaQueryWrapper<SendBoxRecordCheckinfo> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSendBoxRecordCheckinfos(String[] sendBoxRecordCheckinfoIds) {
        List<String> list = Arrays.asList(sendBoxRecordCheckinfoIds);
        this.removeByIds(list);
    }
}
