package com.hero.sms.service.channel;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.PayChannel;

/**
 * 支付通道 Service接口
 *
 * @author Administrator
 * @date 2020-03-12 11:02:02
 */
public interface IPayChannelService extends IService<PayChannel> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param payChannel payChannel
     * @return IPage<PayChannel>
     */
    IPage<PayChannel> findPayChannels(QueryRequest request, PayChannel payChannel);

    /**
     * 查询（所有）
     *
     * @param payChannel payChannel
     * @return List<PayChannel>
     */
    List<PayChannel> findPayChannels(PayChannel payChannel);

    /**
     * 新增
     *
     * @param payChannel payChannel
     */
    void createPayChannel(PayChannel payChannel);

    /**
     * 修改
     *
     * @param payChannel payChannel
     */
    void updatePayChannel(PayChannel payChannel);

    /**
     * 删除
     *
     * @param payChannel payChannel
     */
    void deletePayChannel(PayChannel payChannel);

    /**
    * 删除
    *
    * @param payChannelIds payChannelIds
    */
    void deletePayChannels(String[] payChannelIds);
}
