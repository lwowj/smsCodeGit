package com.hero.sms.service.channel;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.SmsChannelProperty;

/**
 * 短信通道属性 Service接口
 *
 * @author Administrator
 * @date 2020-03-08 17:35:16
 */
public interface ISmsChannelPropertyService extends IService<SmsChannelProperty> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param smsChannelProperty smsChannelProperty
     * @return IPage<SmsChannelProperty>
     */
    IPage<SmsChannelProperty> findSmsChannelPropertys(QueryRequest request, SmsChannelProperty smsChannelProperty);

    /**
     * 查询（所有）
     *
     * @param smsChannelProperty smsChannelProperty
     * @return List<SmsChannelProperty>
     */
    List<SmsChannelProperty> findSmsChannelPropertys(SmsChannelProperty smsChannelProperty);

    /**
     * 新增
     *
     * @param smsChannelProperty smsChannelProperty
     */
    void createSmsChannelProperty(SmsChannelProperty smsChannelProperty);

    /**
     * 修改
     *
     * @param smsChannelProperty smsChannelProperty
     */
    void updateSmsChannelProperty(SmsChannelProperty smsChannelProperty);

    /**
     * 删除
     *
     * @param smsChannelProperty smsChannelProperty
     */
    void deleteSmsChannelProperty(SmsChannelProperty smsChannelProperty);

    /**
    * 删除
    *
    * @param smsChannelPropertyIds smsChannelPropertyIds
    */
    void deleteSmsChannelPropertys(String[] smsChannelPropertyIds);

    List<SmsChannelProperty> queryListByChannelId(Integer id);

    void updateSmsChannelProperties(List<SmsChannelProperty> smsChannelProperties, String username);

	List<SmsChannelProperty> queryListByChannelCode(String channelCode);
}
