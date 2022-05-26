package com.hero.sms.service.message;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendBoxRecordCheckinfo;

/**
 * 发件箱分拣发送记录提交中间校验表 Service接口
 *
 * @author Administrator
 * @date 2021-01-13 18:01:35
 */
public interface ISendBoxRecordCheckinfoService extends IService<SendBoxRecordCheckinfo> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendBoxRecordCheckinfo sendBoxRecordCheckinfo
     * @return IPage<SendBoxRecordCheckinfo>
     */
    IPage<SendBoxRecordCheckinfo> findSendBoxRecordCheckinfos(QueryRequest request, SendBoxRecordCheckinfo sendBoxRecordCheckinfo);

    /**
     * 查询（所有）
     *
     * @param sendBoxRecordCheckinfo sendBoxRecordCheckinfo
     * @return List<SendBoxRecordCheckinfo>
     */
    List<SendBoxRecordCheckinfo> findSendBoxRecordCheckinfos(SendBoxRecordCheckinfo sendBoxRecordCheckinfo);

    /**
     * 新增
     *
     * @param sendBoxRecordCheckinfo sendBoxRecordCheckinfo
     */
    void createSendBoxRecordCheckinfo(SendBoxRecordCheckinfo sendBoxRecordCheckinfo);

    /**
     * 修改
     *
     * @param sendBoxRecordCheckinfo sendBoxRecordCheckinfo
     */
    void updateSendBoxRecordCheckinfo(SendBoxRecordCheckinfo sendBoxRecordCheckinfo);

    /**
     * 删除
     *
     * @param sendBoxRecordCheckinfo sendBoxRecordCheckinfo
     */
    void deleteSendBoxRecordCheckinfo(SendBoxRecordCheckinfo sendBoxRecordCheckinfo);

    /**
    * 删除
    *
    * @param sendBoxRecordCheckinfoIds sendBoxRecordCheckinfoIds
    */
    void deleteSendBoxRecordCheckinfos(String[] sendBoxRecordCheckinfoIds);
}
