package com.hero.sms.mapper.rechargeOrder;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代理充值订单 Mapper
 *
 * @author Administrator
 * @date 2020-03-12 18:00:59
 */
public interface AgentRechargeOrderMapper extends BaseMapper<AgentRechargeOrder> {

    IPage<AgentRechargeOrder> selectByAgentRechargeOrderPage(Page page, @Param(Constants.WRAPPER) Wrapper wrapper);

    List<StatisticBean> statisticRechargeAmount(@Param(Constants.WRAPPER) Wrapper wrapper);
}
