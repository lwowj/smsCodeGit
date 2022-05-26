package com.hero.sms.service.channel;

import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.SmsChannelGroup;

import com.hero.sms.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 通道分组表 Service接口
 *
 * @author Administrator
 * @date 2020-06-20 22:38:31
 */
public interface ISmsChannelGroupService extends IService<SmsChannelGroup> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param smsChannelGroup smsChannelGroup
     * @return IPage<SmsChannelGroup>
     */
    IPage<SmsChannelGroup> findSmsChannelGroups(QueryRequest request, SmsChannelGroup smsChannelGroup);

    /**
     * 查询（所有）
     *
     * @param smsChannelGroup smsChannelGroup
     * @return List<SmsChannelGroup>
     */
    List<SmsChannelGroup> findSmsChannelGroups(SmsChannelGroup smsChannelGroup);

    /**
     * 新增
     *
     * @param smsChannelGroup smsChannelGroup
     */
    void createSmsChannelGroup(SmsChannelGroup smsChannelGroup);

    /**
     * 修改
     *
     * @param smsChannelGroup smsChannelGroup
     */
    void updateSmsChannelGroup(SmsChannelGroup smsChannelGroup);

    /**
     * 删除
     *
     * @param smsChannelGroup smsChannelGroup
     */
    void deleteSmsChannelGroup(SmsChannelGroup smsChannelGroup);

    /**
    * 删除
    *
    * @param smsChannelGroupIds smsChannelGroupIds
    */
    void deleteSmsChannelGroups(String[] smsChannelGroupIds);

    void incrementSaveSmsChannelGroup(Integer smsChannelId, String groupIds) throws ServiceException;
}
