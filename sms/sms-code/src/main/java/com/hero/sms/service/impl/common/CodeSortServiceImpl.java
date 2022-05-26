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
import com.hero.sms.entity.common.CodeSort;
import com.hero.sms.mapper.common.CodeSortMapper;
import com.hero.sms.service.common.ICodeSortService;

/**
 * 代码分类 Service实现
 *
 * @author MrJac
 * @date 2020-03-04 16:53:27
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CodeSortServiceImpl extends ServiceImpl<CodeSortMapper, CodeSort> implements ICodeSortService {

    @Override
    public IPage<CodeSort> findCodeSorts(QueryRequest request, CodeSort codeSort) {
        LambdaQueryWrapper<CodeSort> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(codeSort.getName())) {
            queryWrapper.like(CodeSort::getName,codeSort.getName());
        }
        Page<CodeSort> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<CodeSort> findCodeSorts(CodeSort codeSort) {
	    LambdaQueryWrapper<CodeSort> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createCodeSort(CodeSort codeSort) {
        codeSort.setCreateTime(new Date());
        this.save(codeSort);
    }

    @Override
    @Transactional
    public void updateCodeSort(CodeSort codeSort) {
        this.saveOrUpdate(codeSort);
    }

    @Override
    @Transactional
    public void deleteCodeSorts(String[] codeSortIds) {
        List<String> list = Arrays.asList(codeSortIds);
        this.removeByIds(list);
	}

    @Override
    public CodeSort findById(String id) {
        return this.baseMapper.selectById(id);
    }
}
