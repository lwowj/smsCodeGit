package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SmsTemplate;

/**
 * 短信模板表 Service接口
 *
 * @author Administrator
 * @date 2020-03-11 20:08:30
 */
public interface ISmsTemplateService extends IService<SmsTemplate> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param smsTemplate smsTemplate
     * @return IPage<SmsTemplate>
     */
    IPage<SmsTemplate> findSmsTemplates(QueryRequest request, SmsTemplate smsTemplate);

    /**
     * 查询（所有）
     *
     * @param smsTemplate smsTemplate
     * @return List<SmsTemplate>
     */
    List<SmsTemplate> findSmsTemplates(SmsTemplate smsTemplate);

    /**
     * 新增
     *
     * @param smsTemplate smsTemplate
     */
    void createSmsTemplate(SmsTemplate smsTemplate) throws ServiceException;

    /**
     * 修改
     *
     * @param smsTemplate smsTemplate
     */
    void updateSmsTemplate(SmsTemplate smsTemplate);

    /**
     * 删除
     *
     * @param smsTemplate smsTemplate
     */
    void deleteSmsTemplate(SmsTemplate smsTemplate);

    /**
    * 删除
    *
    * @param smsTemplateIds smsTemplateIds
    */
    void deleteSmsTemplates(String[] smsTemplateIds);

    void deleteSmsTemplates(String[] ids, SmsTemplate deleteParams);
}
