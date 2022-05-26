package com.hero.sms.mapper.rechargeOrder;

import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderExt;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderQuery;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;

import java.util.List;
import java.util.Map;

/**
 * 商户充值订单 Mapper
 *
 * @author Administrator
 * @date 2020-03-12 17:57:48
 */
public interface OrganizationRechargeOrderMapper extends BaseMapper<OrganizationRechargeOrder> {

    IPage<OrganizationRechargeOrder> selectByOrganizationPage(Page page, @Param(Constants.WRAPPER) Wrapper wrapper);

    IPage<OrganizationRechargeOrderExt> extPage(@Param("page") Page<?> page, @Param("query")OrganizationRechargeOrderQuery query);

    List<StatisticBean> statisticRechargeAmount(@Param(Constants.WRAPPER) Wrapper wrapper);
}
