package com.hero.sms.service.impl.channel;

import java.util.Arrays;
import java.util.List;

import com.hero.sms.common.entity.Constant;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.service.RedisHelper;
import com.wuwenze.poi.util.Const;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.NumberPoolGroup;
import com.hero.sms.mapper.channel.NumberPoolGroupMapper;
import com.hero.sms.service.channel.INumberPoolGroupService;

/**
 * 号码池组表 Service实现
 *
 * @author Administrator
 * @date 2020-04-15 21:09:40
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class NumberPoolGroupServiceImpl extends ServiceImpl<NumberPoolGroupMapper, NumberPoolGroup> implements INumberPoolGroupService {

    @Autowired
    private NumberPoolGroupMapper numberPoolGroupMapper;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public IPage<NumberPoolGroup> findNumberPoolGroups(QueryRequest request, NumberPoolGroup numberPoolGroup) {
        LambdaQueryWrapper<NumberPoolGroup> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(numberPoolGroup.getName())){
            queryWrapper.eq(NumberPoolGroup::getName,numberPoolGroup.getName());
        }
        Page<NumberPoolGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<NumberPoolGroup> findNumberPoolGroups(NumberPoolGroup numberPoolGroup) {
	    LambdaQueryWrapper<NumberPoolGroup> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (StringUtils.isNotBlank(numberPoolGroup.getName())){
            queryWrapper.eq(NumberPoolGroup::getName,numberPoolGroup.getName());
        }
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createNumberPoolGroup(NumberPoolGroup numberPoolGroup) {
        this.save(numberPoolGroup);
    }

    @Override
    @Transactional
    public void updateNumberPoolGroup(NumberPoolGroup numberPoolGroup) {
        this.saveOrUpdate(numberPoolGroup);
    }

    @Override
    @Transactional
    public void deleteNumberPoolGroup(NumberPoolGroup numberPoolGroup) throws ServiceException {
        LambdaQueryWrapper<NumberPoolGroup> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
        if (numberPoolGroup.getId() == null ){
            throw new ServiceException("参数缺失 删除失败！");
        }
        //删除组
        wrapper.eq(NumberPoolGroup::getId,numberPoolGroup.getId());
	    this.remove(wrapper);
	    //删除组号码池
	    String key = Constant.PER_KEY_NUMBERGROUP_REDIS + numberPoolGroup.getId();
	    redisHelper.del(key);
	}

    @Override
    @Transactional
    public void deleteNumberPoolGroups(String[] numberPoolGroupIds) {
        List<String> list = Arrays.asList(numberPoolGroupIds);
        this.removeByIds(list);
    }
}
