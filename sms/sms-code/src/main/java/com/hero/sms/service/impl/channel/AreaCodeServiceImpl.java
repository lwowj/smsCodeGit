package com.hero.sms.service.impl.channel;


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
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.channel.AreaCodeExt;
import com.hero.sms.mapper.channel.AreaCodeMapper;
import com.hero.sms.service.channel.IAreaCodeService;

/**
 * 国家地区表 Service实现
 *
 * @author Administrator
 * @date 2022-03-18 15:41:19
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AreaCodeServiceImpl extends ServiceImpl<AreaCodeMapper, AreaCode> implements IAreaCodeService {

    @Autowired
    private AreaCodeMapper areaCodeMapper;

    @Override
    public IPage<AreaCode> findAreaCodes(QueryRequest request, AreaCodeExt areaCode) {
    	
        LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(areaCode.getAreaCodingStr())){
            queryWrapper.like(AreaCode::getAreaCoding,areaCode.getAreaCodingStr());
        }
        if (StringUtils.isNotBlank(areaCode.getInAreaStr())){
            queryWrapper.like(AreaCode::getInArea,areaCode.getInAreaStr());
        }
        if (StringUtils.isNotBlank(areaCode.getOutAreaStr())){
            queryWrapper.like(AreaCode::getOutArea,areaCode.getOutAreaStr());
        }
        if (StringUtils.isNotBlank(areaCode.getAreaNameStr())){
            queryWrapper.like(AreaCode::getAreaName,areaCode.getAreaNameStr());
        }
  		// TODO 设置查询条件
    	if (StringUtils.isNotBlank(areaCode.getAreaCoding())){
            queryWrapper.eq(AreaCode::getAreaCoding,areaCode.getAreaCoding());
        }
        if (StringUtils.isNotBlank(areaCode.getInArea())){
            queryWrapper.eq(AreaCode::getInArea,areaCode.getInArea());
        }
        if (StringUtils.isNotBlank(areaCode.getOutArea())){
            queryWrapper.eq(AreaCode::getOutArea,areaCode.getOutArea());
        }
        if (StringUtils.isNotBlank(areaCode.getAreaName())){
            queryWrapper.eq(AreaCode::getAreaName,areaCode.getAreaName());
        }
        queryWrapper.orderByAsc(AreaCode::getOrderNum);
        // TODO 设置查询条件
        Page<AreaCode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AreaCode> findAreaCodes(AreaCode areaCode) {
    	LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
  		// TODO 设置查询条件
    	if (StringUtils.isNotBlank(areaCode.getAreaCoding())){
            queryWrapper.eq(AreaCode::getAreaCoding,areaCode.getAreaCoding());
        }
        if (StringUtils.isNotBlank(areaCode.getInArea())){
            queryWrapper.eq(AreaCode::getInArea,areaCode.getInArea());
        }
        if (StringUtils.isNotBlank(areaCode.getOutArea())){
            queryWrapper.eq(AreaCode::getOutArea,areaCode.getOutArea());
        }
        if (StringUtils.isNotBlank(areaCode.getAreaName())){
            queryWrapper.eq(AreaCode::getAreaName,areaCode.getAreaName());
        }
        queryWrapper.orderByAsc(AreaCode::getOrderNum);
  		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createAreaCode(AreaCode areaCode) throws ServiceException {
        if (StringUtils.isBlank(areaCode.getInArea())){
            throw new ServiceException("系统区号不能为空！");
        }
        if(!areaCode.getInArea().startsWith("+"))
    	{
    		areaCode.setInArea("+"+areaCode.getInArea());
    	}
        if (StringUtils.isBlank(areaCode.getOutArea())){
            throw new ServiceException("对接区号不能为空！");
        }
        if(!areaCode.getOutArea().startsWith("+"))
    	{
    		areaCode.setOutArea("+"+areaCode.getOutArea());
    	}
        if (StringUtils.isBlank(areaCode.getAreaCoding())){
            throw new ServiceException("地区编码不能为空！");
        }
      //必填项检测
        if (StringUtils.isBlank(areaCode.getAreaName())){
            throw new ServiceException("地区名称不能为空！");
        }
        AreaCode isAreaCode = getOne(areaCode);
        if(isAreaCode!=null)
        {
        	throw new ServiceException("系统对应对接区号已存在！");
        }
        else
        {
        	this.save(areaCode);
        }
    }

    @Override
    @Transactional
    public void updateAreaCode(AreaCode areaCode) throws ServiceException {
    	//必填项检测
        if (StringUtils.isBlank(areaCode.getAreaName())){
            throw new ServiceException("地区名称不能为空！");
        }
        if (StringUtils.isBlank(areaCode.getInArea())){
            throw new ServiceException("系统区号不能为空！");
        }
        if(!areaCode.getInArea().startsWith("+"))
    	{
    		areaCode.setInArea("+"+areaCode.getInArea());
    	}
        if (StringUtils.isBlank(areaCode.getOutArea())){
            throw new ServiceException("对接区号不能为空！");
        }
        if(!areaCode.getOutArea().startsWith("+"))
    	{
    		areaCode.setOutArea("+"+areaCode.getOutArea());
    	}
        if (StringUtils.isBlank(areaCode.getAreaCoding())){
            throw new ServiceException("地区编码不能为空！");
        }
        AreaCode isAreaCode = getOneForId(areaCode);
        if(isAreaCode!=null)
        {
        	if(isAreaCode.getInArea().equals(areaCode.getInArea())&&isAreaCode.getOutArea().equals(areaCode.getOutArea()))
            {
        		this.saveOrUpdate(areaCode);
            }
        	else 
        	{
        		AreaCode checkAreaCode = getOne(areaCode);
        		if(checkAreaCode!=null)
        		{
        			throw new ServiceException("系统对应对接区号已存在！");
        		}
        		else
        		{
        			this.saveOrUpdate(areaCode);
        		}
        	}
        }
        else
        {
        	this.saveOrUpdate(areaCode);
        }
    }

	@Override
	public AreaCode getOne(AreaCode areaCode) {
		LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AreaCode::getInArea, areaCode.getInArea());
		queryWrapper.eq(AreaCode::getOutArea, areaCode.getOutArea());
		return this.getOne(queryWrapper, true);
	}
	
	@Override
	public AreaCode getOneForId(AreaCode areaCode) {
		LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AreaCode::getId, areaCode.getId());
		return this.getOne(queryWrapper, true);
	}
	
    @Override
    @Transactional
    public void deleteAreaCode(AreaCode areaCode) {
        LambdaQueryWrapper<AreaCode> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteAreaCodes(String[] areaCodeIds) {
        List<String> list = Arrays.asList(areaCodeIds);
        this.removeByIds(list);
    }
}
