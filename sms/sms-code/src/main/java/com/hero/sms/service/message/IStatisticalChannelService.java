package com.hero.sms.service.message;

import com.hero.sms.entity.message.StatisticalChannel;

import com.hero.sms.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.entity.message.StatisticalChannelQuery;

import java.util.List;
import java.util.Map;

/**
 * 按照通道统计 Service接口
 *
 * @author Administrator
 * @date 2020-05-28 15:59:07
 */
public interface IStatisticalChannelService extends IService<StatisticalChannel> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param statisticalChannel statisticalChannel
     * @return IPage<StatisticalChannel>
     */
    IPage<StatisticalChannel> findStatisticalChannels(QueryRequest request, StatisticalChannelQuery statisticalChannel);

    /**
     * 查询（所有）
     *
     * @param statisticalChannel statisticalChannel
     * @return List<StatisticalChannel>
     */
    List<StatisticalChannel> findStatisticalChannels(StatisticalChannelQuery statisticalChannel);

    /**
     * 新增
     *
     * @param statisticalChannel statisticalChannel
     */
    void createStatisticalChannel(StatisticalChannel statisticalChannel);

    /**
     * 修改
     *
     * @param statisticalChannel statisticalChannel
     */
    void updateStatisticalChannel(StatisticalChannel statisticalChannel);

    /**
     * 删除
     *
     * @param statisticalChannel statisticalChannel
     */
    void deleteStatisticalChannel(StatisticalChannelQuery statisticalChannel);

    /**
    * 删除
    *
    * @param statisticalChannelIds statisticalChannelIds
    */
    void deleteStatisticalChannels(String[] statisticalChannelIds);

    List<Map<String,Object>> sumStatisticalChannels(StatisticalChannelQuery statisticalChannel);
}
