package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalAgent;
import com.hero.sms.entity.message.StatisticalAgentQuery;

/**
 * 按照代理统计 Service接口
 *
 * @author Administrator
 * @date 2020-03-20 14:55:46
 */
public interface IStatisticalAgentService extends IService<StatisticalAgent> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param statisticalAgent statisticalAgent
     * @return IPage<StatisticalAgent>
     */
    IPage<StatisticalAgent> findStatisticalAgents(QueryRequest request, StatisticalAgentQuery statisticalAgent);

    /**
     * 查询（所有）
     *
     * @param statisticalAgent statisticalAgent
     * @return List<StatisticalAgent>
     */
    List<StatisticalAgent> findStatisticalAgents(StatisticalAgentQuery statisticalAgent);

    /**
     * 新增
     *
     * @param statisticalAgent statisticalAgent
     */
    void createStatisticalAgent(StatisticalAgent statisticalAgent);

    /**
     * 修改
     *
     * @param statisticalAgent statisticalAgent
     */
    void updateStatisticalAgent(StatisticalAgentQuery statisticalAgent);

    /**
     * 删除
     *
     * @param statisticalAgent statisticalAgent
     */
    void deleteStatisticalAgent(StatisticalAgentQuery statisticalAgent);

    /**
    * 删除
    *
    * @param statisticalAgentIds statisticalAgentIds
    */
    void deleteStatisticalAgents(String[] statisticalAgentIds);
}
