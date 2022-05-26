package com.hero.sms.service.message;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecordCheckinfoPlan;
import com.hero.sms.entity.message.SendRecordCheckinfoPlanQuery;

/**
 * 发送记录提交中间校验表 Service接口
 *
 * @author Administrator
 * @date 2022-05-02 20:49:18
 */
public interface ISendRecordCheckinfoPlanService extends IService<SendRecordCheckinfoPlan> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendRecordCheckinfoPlan sendRecordCheckinfoPlan
     * @return IPage<SendRecordCheckinfoPlan>
     */
    IPage<SendRecordCheckinfoPlan> findSendRecordCheckinfoPlans(QueryRequest request, SendRecordCheckinfoPlan sendRecordCheckinfoPlan);

    /**
     * 查询（所有）
     *
     * @param sendRecordCheckinfoPlan sendRecordCheckinfoPlan
     * @return List<SendRecordCheckinfoPlan>
     */
    List<SendRecordCheckinfoPlan> findSendRecordCheckinfoPlans(SendRecordCheckinfoPlan sendRecordCheckinfoPlan);

    /**
     * 新增
     *
     * @param sendRecordCheckinfoPlan sendRecordCheckinfoPlan
     */
    void createSendRecordCheckinfoPlan(SendRecordCheckinfoPlan sendRecordCheckinfoPlan);

    /**
     * 修改
     *
     * @param sendRecordCheckinfoPlan sendRecordCheckinfoPlan
     */
    void updateSendRecordCheckinfoPlan(SendRecordCheckinfoPlan sendRecordCheckinfoPlan);

    /**
     * 删除
     *
     * @param sendRecordCheckinfoPlan sendRecordCheckinfoPlan
     */
    void deleteSendRecordCheckinfoPlan(SendRecordCheckinfoPlan sendRecordCheckinfoPlan);

    /**
    * 删除
    *
    * @param sendRecordCheckinfoPlanIds sendRecordCheckinfoPlanIds
    */
    void deleteSendRecordCheckinfoPlans(String[] sendRecordCheckinfoPlanIds);
    
    void updateSendRecordCheckinfoPlanBatch(SendRecordCheckinfoPlanQuery sendRecordCheckinfoPlan);
}
