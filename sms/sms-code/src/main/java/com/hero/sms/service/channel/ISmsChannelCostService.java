package com.hero.sms.service.channel;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.SmsChannelCost;

/**
 * 通道资费 Service接口
 *
 * @author Administrator
 * @date 2020-03-10 15:27:23
 */
public interface ISmsChannelCostService extends IService<SmsChannelCost> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param smsChannelCost smsChannelCost
     * @return IPage<SmsChannelCost>
     */
    IPage<SmsChannelCost> findSmsChannelCosts(QueryRequest request, SmsChannelCost smsChannelCost);

    /**
     * 查询（所有）
     *
     * @param smsChannelCost smsChannelCost
     * @return List<SmsChannelCost>
     */
    List<SmsChannelCost> findSmsChannelCosts(SmsChannelCost smsChannelCost);

    /**
     * 新增
     *
     * @param smsChannelCost smsChannelCost
     */
    void createSmsChannelCost(SmsChannelCost smsChannelCost) throws ServiceException;

    /**
     * 修改
     *
     * @param smsChannelCost smsChannelCost
     */
    void updateSmsChannelCost(SmsChannelCost smsChannelCost) throws ServiceException;

    /**
     * 删除
     *
     * @param smsChannelCost smsChannelCost
     */
    void deleteSmsChannelCost(SmsChannelCost smsChannelCost);

    /**
    * 删除
    *
    * @param smsChannelCostIds smsChannelCostIds
    */
    void deleteSmsChannelCosts(String[] smsChannelCostIds);

    List<SmsChannelCost> queryListByChannelId(Integer id);
}
