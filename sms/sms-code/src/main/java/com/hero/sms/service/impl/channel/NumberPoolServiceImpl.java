package com.hero.sms.service.impl.channel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hero.sms.common.entity.Constant;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.entity.channel.NumberPool;
import com.hero.sms.entity.channel.NumberPoolGroup;
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.enums.channel.NumberPoolPolicyEnums;
import com.hero.sms.enums.channel.SmsChannelPushPropertyNameEnums;
import com.hero.sms.mapper.channel.NumberPoolMapper;
import com.hero.sms.service.channel.INumberPoolGroupService;
import com.hero.sms.service.channel.INumberPoolService;
import com.hero.sms.service.channel.ISmsChannelPropertyService;

import lombok.extern.slf4j.Slf4j;

/**
 * 号码池 Service实现
 *
 * @author Administrator
 * @date 2020-04-15 21:08:21
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class NumberPoolServiceImpl extends ServiceImpl<NumberPoolMapper, NumberPool> implements INumberPoolService {

    @Autowired
    private NumberPoolMapper numberPoolMapper;

    @Autowired
    private INumberPoolGroupService numberPoolGroupService;

    @Autowired
    private ISmsChannelPropertyService smsChannelPropertyService;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public IPage<NumberPool> findNumberPools(QueryRequest request, NumberPool numberPool) {
        LambdaQueryWrapper<NumberPool> queryWrapper = new LambdaQueryWrapper<>();
        if (numberPool.getState() != null){
            queryWrapper.eq(NumberPool::getState,numberPool.getState());
        }
        if (numberPool.getGroupId() != null){
            queryWrapper.eq(NumberPool::getGroupId,numberPool.getGroupId());
        }
        if (StringUtils.isNotBlank(numberPool.getPhoneNumber())){
            queryWrapper.eq(NumberPool::getPhoneNumber,numberPool.getPhoneNumber());
        }
        // TODO 设置查询条件
        Page<NumberPool> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<NumberPool> findNumbers(QueryRequest request, NumberPool numberPool){
        IPage<NumberPool> result = new Page<>();
        if (numberPool.getGroupId() == null){
            return result;
        }
        String  key = Constant.PER_KEY_NUMBERGROUP_REDIS + numberPool.getGroupId();
        int start = (request.getPageNum()-1)* request.getPageSize();
        int end = request.getPageSize() + start;
        Long total = 0L;
        List<NumberPool> datas = Lists.newArrayList();
        if (StringUtils.isNotBlank(numberPool.getPhoneNumber())){
            List<Object> records = redisHelper.lGet(key,0L,-1L);
            List<NumberPool> list = records.stream()
                    .filter(item -> numberPool.getPhoneNumber().equals((String) item))
                    .map(o -> {
                        NumberPool temp = new NumberPool();
                        temp.setPhoneNumber((String) o);
                        temp.setGroupId(numberPool.getGroupId());
                        return temp;
                    })
                    .collect(Collectors.toList());
            total = new Long(list.size());

            while (start > total){
                start -= request.getPageSize();
                end -= request.getPageSize();
            }
            if (end > total){
                end = total.intValue();
            }
            datas = list.subList(start,end);
        }else {
            total = redisHelper.lGetListSize(key);
            if (end > total){
                end = total.intValue();
            }
            List<Object> records = redisHelper.lGet(key, new Long(start), new Long(end));
            datas = records.stream()
                    .map(o -> {
                        NumberPool temp = new NumberPool();
                        temp.setPhoneNumber((String) o);
                        temp.setGroupId(numberPool.getGroupId());
                        return temp;
                    })
                    .collect(Collectors.toList());
        }
        result.setTotal(total);
        result.setRecords(datas);
        result.setPages((long) Math.ceil(total/request.getPageSize()));
        result.setSize(request.getPageSize());
        return result;
    }

    @Override
    public List<NumberPool> findNumberPools(NumberPool numberPool) {
	    LambdaQueryWrapper<NumberPool> queryWrapper = new LambdaQueryWrapper<>();
        if (numberPool.getState() != null){
            queryWrapper.eq(NumberPool::getState,numberPool.getState());
        }
        if (numberPool.getGroupId() != null){
            queryWrapper.eq(NumberPool::getGroupId,numberPool.getGroupId());
        }
        if (StringUtils.isNotBlank(numberPool.getPhoneNumber())){
            queryWrapper.eq(NumberPool::getPhoneNumber,numberPool.getPhoneNumber());
        }
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createNumberPool(NumberPool numberPool) throws ServiceException {
        if (numberPool.getGroupId() == null) throw new ServiceException("池组不能为空");
        if (StringUtils.isBlank(numberPool.getPhoneNumber()))throw new ServiceException("手机号码不能为空");
        String key = Constant.PER_KEY_NUMBERGROUP_REDIS + numberPool.getGroupId();
        redisHelper.lSet(key,numberPool.getPhoneNumber());
    }

    @Override
    @Transactional
    public void updateNumberPool(NumberPool numberPool) {
        this.saveOrUpdate(numberPool);
    }

    @Override
    @Transactional
    public int deleteNumberPool(NumberPool numberPool) {
        if (numberPool.getGroupId() == null) return 0;
        if (StringUtils.isBlank(numberPool.getPhoneNumber()))return 0;
        String key = Constant.PER_KEY_NUMBERGROUP_REDIS + numberPool.getGroupId();
        return redisHelper.lRemove(key,1L,numberPool.getPhoneNumber()).intValue();
	}

    @Override
    @Transactional
    public void deleteNumberPools(String[] numberPoolIds) {
        List<String> list = Arrays.asList(numberPoolIds);
        this.removeByIds(list);
    }

    @Override
    public void batchCreate(List<String> numberList, Integer groupId, Integer state) throws ServiceException {
        int size = numberList.size();
        if (size < 1){
            throw new ServiceException("无有效手机号码，保存失败！");
        }
        if (state == null){
            throw new ServiceException("状态[state]不能为空，保存失败！");
        }
        NumberPoolGroup group = this.numberPoolGroupService.getById(groupId);
        if (group == null){
            throw new ServiceException("号码池组不存在,保存失败！");
        }

        //转成对象
        List<NumberPool> numberPools = numberList.stream()
                .map( s -> {
                    NumberPool numberPool = new NumberPool();
                    numberPool.setGroupId(groupId);
                    numberPool.setState(state);
                    numberPool.setPhoneNumber(s);
                    return numberPool;
                }).collect(Collectors.toList());

        //分段保存，使用异步
        ((INumberPoolService) AopContext.currentProxy()).partitionSave(numberPools,1000);
    }

    @Async
    @Override
    public void partitionSave(List<NumberPool> numberPools, int partirionSize){
        log.info(String.format("【号码池导入】分段保存异步任务开始,本次导入号码数：%s",numberPools.size()));
        long start = System.currentTimeMillis();
        //分段保存
        Lists.partition(numberPools, partirionSize).stream().forEach( list -> {
            this.saveBatch(list);
        });
        log.info(String.format("【号码池导入】分段保存异步任务结束,本次导入耗时：%s ms",System.currentTimeMillis() - start));
    }


    @Override
    public String randomNumberPoolBySmsChannelId(Integer smsChannelId){

        if (smsChannelId == null) return null;
        LambdaQueryWrapper<SmsChannelProperty> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsChannelProperty::getSmsChannelId,smsChannelId);
        List<SmsChannelProperty> channelPropertys = this.smsChannelPropertyService.list(queryWrapper);

        Map<String, String> map = channelPropertys.stream()
                .collect(Collectors.toMap(SmsChannelProperty::getName, SmsChannelProperty::getValue));

        String numberpoolState = map.get(SmsChannelPushPropertyNameEnums.OpenNumberPool.getCode());
        if (StringUtils.isBlank(numberpoolState)) return null;//通道没有设置号码池状态 直接返回空
        int numberpoolPolicy = Integer.valueOf(numberpoolState).intValue();
        if (numberpoolPolicy == NumberPoolPolicyEnums.FAIL.getCode().intValue()){
            //设置成关闭   直接返回null
            return null;
        }
        String numberPoolGroup = map.get(SmsChannelPushPropertyNameEnums.NumberPoolGroup.getCode());
        if (StringUtils.isBlank(numberPoolGroup)){
            //通道没有配置号码词组  直接返回null
            return null;
        }
        //根据策略返回号码
        String key = Constant.PER_KEY_NUMBERGROUP_REDIS + numberPoolGroup;
        if (numberpoolPolicy == NumberPoolPolicyEnums.POLLING.getCode().intValue()){
            return (String) redisHelper.lPopFirstThanPushLast(key);
        }else if (numberpoolPolicy == NumberPoolPolicyEnums.RANDOM.getCode().intValue()){
            Long total = redisHelper.lGetListSize(key);
            Long ramdom = RandomUtils.nextLong(0L,total);
            return (String) redisHelper.lGetIndex(key,ramdom);
        }else {
            return null;
        }
    }
    @Override
    public void batchCreate2Redis(List<String> numberList, Integer groupId) {
        List<Object> list = numberList.stream().map(item -> (Object)item).collect(Collectors.toList());
        redisHelper.lSet(Constant.PER_KEY_NUMBERGROUP_REDIS + groupId,list);
    }
}
