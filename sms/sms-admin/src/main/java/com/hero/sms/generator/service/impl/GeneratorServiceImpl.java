package com.hero.sms.generator.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.common.entity.Constant;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.SortUtil;
import com.hero.sms.generator.entity.Column;
import com.hero.sms.generator.entity.Table;
import com.hero.sms.generator.mapper.GeneratorMapper;
import com.hero.sms.generator.service.IGeneratorService;

/**
 * @author Administrator
 */
@Service
public class GeneratorServiceImpl implements IGeneratorService {
    @Autowired
    private GeneratorMapper generatorMapper;

    @Override
    public List<String> getDatabases(String databaseType) {
        return generatorMapper.getDatabases(databaseType);
    }

    @Override
    public IPage<Table> getTables(String tableName, QueryRequest request, String databaseType, String schemaName) {
        Page<Table> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "createTime", Constant.ORDER_ASC, false);
        return generatorMapper.getTables(page, tableName, databaseType, schemaName);
    }

    @Override
    public List<Column> getColumns(String databaseType, String schemaName, String tableName) {
        return generatorMapper.getColumns(databaseType, schemaName, tableName);
    }
}
