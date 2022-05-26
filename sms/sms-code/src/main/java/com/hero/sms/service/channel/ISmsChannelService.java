package com.hero.sms.service.channel;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.*;
import com.hero.sms.entity.message.SendRecord;

/**
 * 短信通道 Service接口
 *
 * @author Administrator
 * @date 2020-03-08 17:35:03
 */
public interface ISmsChannelService extends IService<SmsChannel> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param smsChannel smsChannel
     * @return IPage<SmsChannel>
     */
    IPage<SmsChannel> findSmsChannels(QueryRequest request, SmsChannel smsChannel);

    IPage<SmsChannelExtGroup> findSmsChannelsExtGroups(QueryRequest request, SmsChannelQuery smsChannel);

    /**
     * 查询（所有）
     *
     * @param smsChannel smsChannel
     * @return List<SmsChannel>
     */
    List<SmsChannel> findSmsChannels(SmsChannel smsChannel);

    /**
     * 获取通道和通道属性
     * @param smsChannel
     * @return
     */
	List<SmsChannelExt> findListContainProperty(SmsChannel smsChannel);
	
    /**
     * 新增
     *
     * @param smsChannel smsChannel
     */
    void createSmsChannel(SmsChannel smsChannel) throws ServiceException;

    /**
     * 修改
     *
     * @param smsChannel smsChannel
     */
    void updateSmsChannel(SmsChannel smsChannel) throws ServiceException;

    List<GatewayConnectInfo> getGatewayConnectInfos(Integer id) throws ServiceException;

    /**
     * 删除
     *
     * @param smsChannel smsChannel
     */
    void deleteSmsChannel(SmsChannel smsChannel);

    /**
    * 删除
    *
    * @param smsChannelIds smsChannelIds
    */
    void deleteSmsChannels(String[] smsChannelIds);

    /**
     * 根据id获取通道和通道属性
     * @param id
     * @return
     */
    SmsChannelExt findContainPropertyById(Integer id);
    
    /**
     * 根据code获取通道和通道属性
     * @param id
     * @return
     */
    SmsChannelExt findContainPropertyByCode(String code);

    /**
     * 根据标识获取通道
     * @param code
     * @return
     */
	SmsChannel getByCode(String code);

    FebsResponse testSmsChannel(String channelCode, SendRecord sendRecord) throws ServiceException;

    void updateSmsChannelStatus(Integer channelId, Integer state) throws ServiceException;
    
    void updateSmsChannelStatusForInvalid(Integer channelId) throws ServiceException;
    
    /**
     * 查询配置对应区域资费的通道
     * @param smsChannel
     * @return
     */
    List<SmsChannel> findSmsChannelsForArea(SmsChannel smsChannel);
}
