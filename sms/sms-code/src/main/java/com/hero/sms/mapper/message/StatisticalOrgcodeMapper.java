package com.hero.sms.mapper.message;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.entity.message.StatisticalOrgcode;
import com.hero.sms.entity.message.StatisticalOrgcodeExt;
import com.hero.sms.entity.message.StatisticalOrgcodeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 按照商户统计 Mapper
 *
 * @author Administrator
 * @date 2020-03-16 16:35:02
 */
public interface StatisticalOrgcodeMapper extends BaseMapper<StatisticalOrgcode> {

    IPage<StatisticalOrgcode> sumStatisticalOrgcodes(@Param("page") Page<?> page, @Param("stat") StatisticalOrgcodeQuery stat);

    IPage<StatisticalOrgcode> sumStatisticalOrgcodesByOrg(@Param("page") Page<?> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    List<StatisticalOrgcode> sumStatisticalOrgcodesByOrg(@Param(Constants.WRAPPER) Wrapper wrapper);

    IPage<StatisticalOrgcodeExt> sumStatisticalBusiness(@Param("page") Page<?> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    List<StatisticalOrgcodeExt> sumStatisticalBusinessList(@Param(Constants.WRAPPER) Wrapper wrapper);

    IPage<StatisticalOrgcodeExt> selectStatisticalAndCost(@Param("page") Page<?> page, @Param("stat") StatisticalOrgcodeQuery stat);

    List<StatisticalOrgcodeExt> selectStatisticalAndCostList(@Param("stat") StatisticalOrgcodeQuery stat);

}
