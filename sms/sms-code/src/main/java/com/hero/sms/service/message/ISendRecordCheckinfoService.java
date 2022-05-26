package com.hero.sms.service.message;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoQuery;

/**
 * 发送记录提交中间校验表 Service接口
 *
 * @author Administrator
 * @date 2020-12-21 17:46:28
 */
public interface ISendRecordCheckinfoService extends IService<SendRecordCheckinfo> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendRecordCheckinfo sendRecordCheckinfo
     * @return IPage<SendRecordCheckinfo>
     */
    IPage<SendRecordCheckinfo> findSendRecordCheckinfos(QueryRequest request, SendRecordCheckinfo sendRecordCheckinfo);

    /**
     * 查询（所有）
     *
     * @param sendRecordCheckinfo sendRecordCheckinfo
     * @return List<SendRecordCheckinfo>
     */
    List<SendRecordCheckinfo> findSendRecordCheckinfos(SendRecordCheckinfo sendRecordCheckinfo);

    /**
     * 新增
     *
     * @param sendRecordCheckinfo sendRecordCheckinfo
     */
    void createSendRecordCheckinfo(SendRecordCheckinfo sendRecordCheckinfo);

    /**
     * 修改
     *
     * @param sendRecordCheckinfo sendRecordCheckinfo
     */
    void updateSendRecordCheckinfo(SendRecordCheckinfo sendRecordCheckinfo);

    /**
     * 删除
     *
     * @param sendRecordCheckinfo sendRecordCheckinfo
     */
    void deleteSendRecordCheckinfo(SendRecordCheckinfo sendRecordCheckinfo);

    /**
    * 删除
    *
    * @param sendRecordCheckinfoIds sendRecordCheckinfoIds
    */
    void deleteSendRecordCheckinfos(String[] sendRecordCheckinfoIds);
    
    void updateSendRecordCheckinfoBatch(SendRecordCheckinfoQuery sendRecordCheckinfo);
}
