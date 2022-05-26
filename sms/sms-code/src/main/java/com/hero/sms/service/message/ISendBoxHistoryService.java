package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;

/**
 * 历史发件箱 Service接口
 *
 * @author Administrator
 * @date 2020-03-15 23:31:27
 */
public interface ISendBoxHistoryService extends IService<SendBoxHistory> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendBoxHistory sendBoxHistory
     * @return IPage<SendBoxHistory>
     */
    IPage<SendBoxHistory> findSendBoxHistorys(QueryRequest request, SendBoxHistoryQuery sendBoxHistory);

    String getSmsNumbersByID(Long id);

    /**
     * 查询（所有）
     *
     * @param sendBoxHistory sendBoxHistory
     * @return List<SendBoxHistory>
     */
    List<SendBoxHistory> findSendBoxHistorys(SendBoxHistoryQuery sendBoxHistory);

    /**
     * 新增
     *
     * @param sendBoxHistory sendBoxHistory
     */
    void createSendBoxHistory(SendBoxHistory sendBoxHistory);

    /**
     * 修改
     *
     * @param sendBoxHistory sendBoxHistory
     */
    void updateSendBoxHistory(SendBoxHistoryQuery sendBoxHistory);

    /**
     * 删除
     *
     * @param sendBoxHistory sendBoxHistory
     */
    void deleteSendBoxHistory(SendBoxHistoryQuery sendBoxHistory);

    /**
    * 删除
    *
    * @param sendBoxHistoryIds sendBoxHistoryIds
    */
    void deleteSendBoxHistorys(String[] sendBoxHistoryIds);


    /**
     * 统计记录数
     * @param sendBoxHistory
     * @return
     */
    int countByEntity(SendBoxHistoryQuery sendBoxHistory);
}
