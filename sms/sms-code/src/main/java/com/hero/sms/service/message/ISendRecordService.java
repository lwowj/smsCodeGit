package com.hero.sms.service.message;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordQuery;

import javax.servlet.http.HttpServletResponse;

/**
 * 发送记录 Service接口
 *
 * @author Administrator
 * @date 2020-03-07 23:20:22
 */
public interface ISendRecordService extends IService<SendRecord> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendRecord sendRecord
     * @return IPage<SendRecord>
     */
    IPage<SendRecord> findSendRecords(QueryRequest request, SendRecordQuery sendRecord);

    /**
     * 查询（所有）
     *
     * @param sendRecord sendRecord
     * @return List<SendRecord>
     */
    List<SendRecord> findSendRecords(SendRecordQuery sendRecord);

    /**
     * 新增
     *
     * @param sendRecord sendRecord
     */
    void createSendRecord(SendRecord sendRecord);

    /**
     * 修改
     *
     * @param sendRecord sendRecord
     */
    void updateSendRecord(SendRecord sendRecord);

    /**
     * 删除
     *
     * @param sendRecord sendRecord
     */
    void deleteSendRecord(SendRecordQuery sendRecord);

    /**
    * 删除
    *
    * @param sendRecordIds sendRecordIds
    */
    void deleteSendRecords(String[] sendRecordIds);

    /**
     * 根据发送记录id推送消息
     * @param id
     */
	void pushHttpMsg(Long sendRecordId);

	void pushMsg(String protocolType,Long sendRecordId);
	/**
	 * 根据手机号码和msgid获取记录
	 * @param smsNumber
	 * @param resMsgid
	 * @return
	 */
	SendRecord getByAreaAndNumberAndMsgId(String area,String smsNumber, String resMsgid);

	/**
	 * 2021-10-22
	 * 根据通道ID和msgid获取记录
	 * @param channelId
	 * @param resMsgid
	 * @return
	 */	
	SendRecord getByChannelIdAndMsgId(int channelId, String resMsgid);
	
    int batchNotifyMsgState(SendRecordQuery sendRecord);

    /**
     * 批量补发推送状态通知给下游
     * @param sendRecordIds
     * @return  count 补发通知的成功数量
     */
    int batchNotifyMsgState(String sendRecordIds);

    //手动批量回执通知
    @Transactional(rollbackFor = Exception.class)
    int batchNotifyMsgState(List<SendRecord> list);

    /**
     * 根据ID集合修改回执状态
     * @param sendRecordIds
     * @param success
     * @return
     */
    int batchUpdateMsgReturnState(String sendRecordIds, Boolean success);

    /**
     * 根据条件批量修改回执状态
     * @param sendRecordQuery  过滤条件
     * @param successRate   成功率
     * @return  更新结果消息概述
     */
    String batchUpdateMsgReturnState(SendRecordQuery sendRecordQuery, BigDecimal successRate);

    @Transactional(rollbackFor = Exception.class)
    int batchUpdateMsgReturnState(List<SendRecord> list, Boolean success);

    @Async
    void exportSendCordFromAdmin(Long userId, SendRecordQuery sendRecord);

    String exportSendRecordFromOrg(Long userId, SendRecordQuery sendRecord);

    int countByQueryEntity(SendRecordQuery sendRecord);
}
