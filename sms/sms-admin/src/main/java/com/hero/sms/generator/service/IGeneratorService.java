package com.hero.sms.generator.service;

import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.generator.entity.Column;
import com.hero.sms.generator.entity.Table;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @author Administrator
 */
public interface IGeneratorService {

    List<String> getDatabases(String databaseType);

    IPage<Table> getTables(String tableName, QueryRequest request, String databaseType, String schemaName);

    List<Column> getColumns(String databaseType, String schemaName, String tableName);
}
