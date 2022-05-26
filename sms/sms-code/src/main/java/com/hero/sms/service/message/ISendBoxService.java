package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.message.ApiSendSmsModel;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SimpleNote;

/**
 * 发件箱 Service接口
 *
 * @author Administrator
 * @date 2020-03-07 15:56:53
 */
public interface ISendBoxService extends IService<SendBox> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sendBox sendBox
     * @return IPage<SendBox>
     */
    IPage<SendBox> findSendBoxs(QueryRequest request, SendBoxQuery sendBox);

    String getSmsNumbersByID(Long id);

    /**
     * 查询（所有）
     *
     * @param sendBox sendBox
     * @return List<SendBox>
     */
    List<SendBox> findSendBoxs(SendBoxQuery sendBox);

    /**
     * 新增
     *
     * @param sendBox sendBox
     * @throws Exception 
     */
    void createSendBox(SendBox sendBox) throws Exception;

    /**
     * 修改
     *
     * @param sendBox sendBox
     */
    void updateSendBox(SendBox sendBox);

    /**
     * 删除
     *
     * @param sendBox sendBox
     */
    void deleteSendBox(SendBox sendBox);

    /**
    * 删除
    *
    * @param sendBoxIds sendBoxIds
    */
    void deleteSendBoxs(String[] sendBoxIds);

	void splitRecord(List<SendBox> sendBoxes);
	
	void splitRecordForTxt(SendBox sendBox, List<String> smsNumberList);

	void splitRecordForExcel(SendBox sendBox, List<Object> excelModels);
	
    /**
     * 根据发件箱id分拣手机号码发送短信
     * @param simpleNote
     */
	void sortingSendBox(SimpleNote simpleNote,String msgId);

    void audit(String sendBoxIds, Integer code,String refuseCause);

    /**
     * 开启事务保存商户消费金额、代理利润金额、发送记录
     */
	void sendBoxForSaveData(SendBox sendBox, List<SendRecord> sendRecords) throws Exception;

	FebsResponse apiSendSMS(ApiSendSmsModel model,String clientIp);

    /**
     * 批量清除定时时间
     * 并将审核状态改为 审核不通过
     * @param sendBoxIds
     * @param isSendingNow
     */
    void cancelTimings(String[] sendBoxIds, boolean isSendingNow);
    
    /**
     * 推送消息
     * @param smsChannels
     * @param sendRecords
     */
    public int pushMsg(List<SmsChannel> smsChannels,List<SendRecord> sendRecords);

    int countByEntity(SendBoxQuery sendBox);
}
