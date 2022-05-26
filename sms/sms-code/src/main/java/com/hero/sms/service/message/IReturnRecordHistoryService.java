package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.history.ReturnRecordHistory;
import com.hero.sms.entity.message.history.ReturnRecordHistoryQuery;

/**
 * 历史回执记录 Service接口
 *
 * @author Administrator
 * @date 2020-03-15 23:31:41
 */
public interface IReturnRecordHistoryService extends IService<ReturnRecordHistory> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param returnRecordHistory returnRecordHistory
     * @return IPage<ReturnRecordHistory>
     */
    IPage<ReturnRecordHistory> findReturnRecordHistorys(QueryRequest request, ReturnRecordHistoryQuery returnRecordHistory);

    /**
     * 查询（所有）
     *
     * @param returnRecordHistory returnRecordHistory
     * @return List<ReturnRecordHistory>
     */
    List<ReturnRecordHistory> findReturnRecordHistorys(ReturnRecordHistoryQuery returnRecordHistory);

    /**
     * 新增
     *
     * @param returnRecordHistory returnRecordHistory
     */
    void createReturnRecordHistory(ReturnRecordHistory returnRecordHistory);

    /**
     * 修改
     *
     * @param returnRecordHistory returnRecordHistory
     */
    void updateReturnRecordHistory(ReturnRecordHistoryQuery returnRecordHistory);

    /**
     * 删除
     *
     * @param returnRecordHistory returnRecordHistory
     */
    void deleteReturnRecordHistory(ReturnRecordHistoryQuery returnRecordHistory);

    /**
    * 删除
    *
    * @param returnRecordHistoryIds returnRecordHistoryIds
    */
    void deleteReturnRecordHistorys(String[] returnRecordHistoryIds);

    Integer countByQueryEntity(ReturnRecordHistoryQuery returnRecordHistory);
}
