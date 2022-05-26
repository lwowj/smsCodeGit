package com.hero.sms.service.message;



import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.history.SendRecordHistory;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;

/**
 * 历史发送记录 Service接口
 *
 * @author Administrator
 * @date 2020-03-15 23:31:38
 */
public interface ISendRecordHistoryService extends IService<SendRecordHistory> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendRecordHistory sendRecordHistory
     * @return IPage<SendRecordHistory>
     */
    IPage<SendRecordHistory> findSendRecordHistorys(QueryRequest request, SendRecordHistoryQuery sendRecordHistory);

    /**
     * 查询（所有）
     *
     * @param sendRecordHistory sendRecordHistory
     * @return List<SendRecordHistory>
     */
    List<SendRecordHistory> findSendRecordHistorys(SendRecordHistoryQuery sendRecordHistory);

    /**
     * 新增
     *
     * @param sendRecordHistory sendRecordHistory
     */
    void createSendRecordHistory(SendRecordHistory sendRecordHistory);

    /**
     * 修改
     *
     * @param sendRecordHistory sendRecordHistory
     */
    void updateSendRecordHistory(SendRecordHistoryQuery sendRecordHistory);

    /**
     * 删除
     *
     * @param sendRecordHistory sendRecordHistory
     */
    void deleteSendRecordHistory(SendRecordHistoryQuery sendRecordHistory);

    /**
    * 删除
    *
    * @param sendRecordHistoryIds sendRecordHistoryIds
    */
    void deleteSendRecordHistorys(String[] sendRecordHistoryIds);

    Integer countByQueryEntity(SendRecordHistoryQuery sendRecordHistory);
}
