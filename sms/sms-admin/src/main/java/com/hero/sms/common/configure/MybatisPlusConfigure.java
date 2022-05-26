package com.hero.sms.common.configure;

import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.parsers.BlockAttackSqlParser;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Configuration
public class MybatisPlusConfigure {
    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        List<ISqlParser> sqlParserList = new ArrayList<>();
        // 攻击 SQL 阻断解析器、加入解析链
        sqlParserList.add(new BlockAttackSqlParser());
        paginationInterceptor.setSqlParserList(sqlParserList);
        // paginationInterceptor.setLimit(你的最大单页限制数量，默认 500 条，小于 0 如 -1 不受限制);
        //导出时用到2W条一次导出，所以这里设置成不限制
        paginationInterceptor.setLimit(-1);
        return paginationInterceptor;
    }
}
