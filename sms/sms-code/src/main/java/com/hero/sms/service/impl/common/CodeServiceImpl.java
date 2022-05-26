package com.hero.sms.service.impl.common;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.mapper.common.CodeMapper;
import com.hero.sms.service.common.ICodeService;

/**
 * 代码表 Service实现
 *
 * @author MrJac
 * @date 2020-03-04 21:15:50
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CodeServiceImpl extends ServiceImpl<CodeMapper, Code> implements ICodeService {

    @Override
    public IPage<Code> findCodes(QueryRequest request, Code code) {
        LambdaQueryWrapper<Code> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(code.getSortCode())){
            queryWrapper.eq(Code::getSortCode,code.getSortCode());
        }
        if (StringUtils.isNotBlank(code.getCode())){
            queryWrapper.eq(Code::getCode,code.getCode());
        }
        if (StringUtils.isNotBlank(code.getParentCode())){
            queryWrapper.eq(Code::getParentCode,code.getParentCode());
        }
        if (StringUtils.isNotBlank(code.getName())){
            queryWrapper.eq(Code::getName,code.getName());
        }
        queryWrapper.orderByDesc(Code::getCreateTime);
        // TODO 设置查询条件
        Page<Code> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Code> findCodes(Code code) {
	    LambdaQueryWrapper<Code> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (StringUtils.isNotBlank(code.getCode())){
            queryWrapper.eq(Code::getCode,code.getCode());
        }
        if (StringUtils.isNotBlank(code.getParentCode())){
            queryWrapper.eq(Code::getParentCode,code.getParentCode());
        }
        if (StringUtils.isNotBlank(code.getName())){
            queryWrapper.eq(Code::getName,code.getName());
        }
        queryWrapper.orderByDesc(Code::getCreateTime);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createCode(Code code) throws ServiceException {

        if (StringUtils.isBlank(code.getSortCode())){
            throw new ServiceException("代码分类不能为空！");
        }
        if (StringUtils.isBlank(code.getCode())){
            throw new ServiceException("字典代码不能为空");
        }
        /**
         * @begin 2021-01-23
         * 新增限制的格式校验
         */
        if("SmsNumberLimitRule".equals(code.getSortCode()))
        {
        	if (StringUtils.isNotBlank(code.getName()))
            {
            	if(!RegexUtil.isNumberLimit(code.getName()))
            	{
            		throw new ServiceException("限制表达式格式不正确！");
            	}
            }
        }
        /**
         * @end
         */
        LambdaQueryWrapper<Code> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Code::getSortCode,code.getSortCode())
                .eq(Code::getCode,code.getCode())
                .eq(Code::getIsDelete,"0");
        int count = count(queryWrapper);
        if (count > 0){
            throw new ServiceException(String.format("字典【分类:%s 代码:%s】已经存在！",code.getSortCode(),code.getCode()));
        }
        code.setCreateTime(new Date());
        code.setIsDelete("0");
        this.save(code);
    }

    @Override
    @Transactional
    public void updateCode(Code code) {
        this.saveOrUpdate(code);
    }

    @Override
    @Transactional
    public void deleteCodes(String[] codeSortIds) {
        List<String> list = Arrays.asList(codeSortIds);
        this.removeByIds(list);
	}
}
