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
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.MobileArea;
import com.hero.sms.mapper.message.MobileAreaMapper;
import com.hero.sms.service.message.IMobileAreaService;

/**
 *  Service实现
 *
 * @author Administrator
 * @date 2020-03-24 21:15:09
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class MobileAreaServiceImpl extends ServiceImpl<MobileAreaMapper, MobileArea> implements IMobileAreaService {

    @Autowired
    private MobileAreaMapper mobileAreaMapper;

    @Override
    public IPage<MobileArea> findMobileAreas(QueryRequest request, MobileArea mobileArea) {
        LambdaQueryWrapper<MobileArea> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<MobileArea> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<MobileArea> findMobileAreas(MobileArea mobileArea) {
	    LambdaQueryWrapper<MobileArea> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createMobileArea(MobileArea mobileArea) {
        this.save(mobileArea);
    }

    @Override
    @Transactional
    public void updateMobileArea(MobileArea mobileArea) {
        this.saveOrUpdate(mobileArea);
    }

    @Override
    @Transactional
    public void deleteMobileArea(MobileArea mobileArea) {
        LambdaQueryWrapper<MobileArea> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteMobileAreas(String[] mobileAreaIds) {
        List<String> list = Arrays.asList(mobileAreaIds);
        this.removeByIds(list);
    }
    
    @Override
    public MobileArea getCacheOne(MobileArea mobileArea) 
    {
    	List<MobileArea> allMobileAreaList = DatabaseCache.getMobileAreaList();
    	if(allMobileAreaList!=null&&allMobileAreaList.size()>0)
    	{
    		for (int i = 0; i < allMobileAreaList.size(); i++) 
    		{
    			MobileArea thismobileArea = allMobileAreaList.get(i);
    			if(thismobileArea.getMobilenumber().equals(mobileArea.getMobilenumber()))
    			{
    				return thismobileArea;
    			}
			}
    	}
        return null;
    }
}
