package com.hero.sms.service.message;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.ReturnRecordQuery;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.entity.message.history.ReturnRecordHistoryQuery;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;

public interface IReportService {

    /**
     * 统计今日商户发送量
     * @param
     * @return
     */
    List<Map<String,Object>> orgSendCountToday();

    /**
     * 今日各省发送数据量统计
     * @return
     */
    List<Map<String,Object>> provinceSendCountToday();

    /**
     * 今日各运营商发送量
     * @return
     */
    List<Map<String,Object>> operatorSendCountToday();

    /**
     * 实时统计发件箱信息
     * @return
     */
    Map<String, Object> statisticSendBoxInfo(SendBoxQuery sendBox);

    /**
     * 实时统计历史发件箱信息
     * @return
     */
    Map<String, Object> statisticSendBoxHistoryInfo(SendBoxHistoryQuery sendBoxHistory);

    /**
     * 实时统计发送记录信息
     * @return
     */
    Map<String, Object> statisticSendRecordInfo(SendRecordQuery sendRecord);

    /**
     * 实时统计历史发送记录信息
     * @return
     */
    Map<String, Object> statisticSendRecordHistoryInfo(SendRecordHistoryQuery SendRecordHistory);

    /**
     * 实时统计发送回执信息
     * @return
     */
    Map<String, Object> statisticReturnRecordInfo(ReturnRecordQuery ReturnRecord);

    /**
     * 实时统计历史发送回执信息
     * @return
     */
    Map<String, Object> statisticReturnRecordHistoryInfo(ReturnRecordHistoryQuery ReturnRecordHistory);

    /**
     * 代理后台实时统计订单情况
     * @param agent
     * @return
     */
    List<Map<String,Object>> sumSendRecordByAgent(Agent agent);


    /**
     * 代理后台实时统计下级代理订单情况
     * @param agent
     * @return
     */

    List<Map<String,Object>> sumLowerSendRecordByAgent(Agent agent);

    IPage<Map<String, Object>> statisticRateSuccessGroupBySendCode(QueryRequest request, SendRecordQuery sendRecord);

    List<Map<String, Object>> statisticRateSuccessGroupBySendCode(SendRecordQuery sendRecord);

    IPage<Map<String, Object>> statisticRateSuccessGroupByContent(QueryRequest request, SendRecordQuery sendRecord);

    List<Map<String, Object>> statisticRateSuccessGroupByContent(SendRecordQuery sendRecord);
}
