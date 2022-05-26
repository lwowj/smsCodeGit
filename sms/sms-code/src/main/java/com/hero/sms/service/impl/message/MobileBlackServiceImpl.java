package com.hero.sms.service.impl.message;

import java.util.Arrays;
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
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.MobileBlack;
import com.hero.sms.mapper.message.MobileBlackMapper;
import com.hero.sms.service.message.IMobileBlackService;

/**
 * 手机号码黑名单 Service实现
 *
 * @author Administrator
 * @date 2020-03-17 01:17:22
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class MobileBlackServiceImpl extends ServiceImpl<MobileBlackMapper, MobileBlack> implements IMobileBlackService {

    @Autowired
    private MobileBlackMapper mobileBlackMapper;

    @Override
    public IPage<MobileBlack> findMobileBlacks(QueryRequest request, MobileBlack mobileBlack) {
        LambdaQueryWrapper<MobileBlack> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(mobileBlack.getNumber())){
            queryWrapper.eq(MobileBlack::getNumber,mobileBlack.getNumber());
        }
        Page<MobileBlack> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<MobileBlack> findMobileBlacks(MobileBlack mobileBlack) {
	    LambdaQueryWrapper<MobileBlack> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (StringUtils.isNotBlank(mobileBlack.getNumber())){
            queryWrapper.eq(MobileBlack::getNumber,mobileBlack.getNumber());
        }
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createMobileBlack(MobileBlack mobileBlack) throws ServiceException {
        if (StringUtils.isBlank(mobileBlack.getArea()) || StringUtils.isBlank(mobileBlack.getNumber())){
            throw new ServiceException("必填项不能为空，请检查!");
        }
        LambdaQueryWrapper<MobileBlack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileBlack::getArea,mobileBlack.getArea());
        queryWrapper.eq(MobileBlack::getNumber,mobileBlack.getNumber());
        int count = this.count(queryWrapper);
        if (count > 1){
            throw new ServiceException("黑名单已存在，添加失败");
        }
        this.save(mobileBlack);
    }

    @Override
    @Transactional
    public void updateMobileBlack(MobileBlack mobileBlack) {
        this.saveOrUpdate(mobileBlack);
    }

    @Override
    @Transactional
    public void deleteMobileBlack(MobileBlack mobileBlack) {
        LambdaQueryWrapper<MobileBlack> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(mobileBlack.getArea())) {
            wrapper.eq(MobileBlack::getArea,mobileBlack.getArea());
        }
        if (StringUtils.isNotBlank(mobileBlack.getNumber())){
            wrapper.eq(MobileBlack::getNumber,mobileBlack.getNumber());
        }
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteMobileBlacks(String[] mobileBlackIds) {
        List<String> list = Arrays.asList(mobileBlackIds);
        this.removeByIds(list);
    }
    
    @Override
    public MobileBlack getCacheOne(MobileBlack mobileBlack) 
    {
    	List<MobileBlack> allMobileBlackList = DatabaseCache.getMobileBlackList();
    	if(allMobileBlackList!=null&&allMobileBlackList.size()>0)
    	{
    		for (int i = 0; i < allMobileBlackList.size(); i++) 
    		{
    			MobileBlack thisMobileBlack = allMobileBlackList.get(i);
    			if(thisMobileBlack.getArea().equals(mobileBlack.getArea())&&thisMobileBlack.getNumber().equals(mobileBlack.getNumber()))
    			{
    				return thisMobileBlack;
    			}
			}
    	}
        return null;
    }
}
