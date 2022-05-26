package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.ReturnRecordQuery;

/**
 * 回执记录 Service接口
 *
 * @author Administrator
 * @date 2020-03-12 00:40:26
 */
public interface IReturnRecordService extends IService<ReturnRecord> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param returnRecord returnRecord
     * @return IPage<ReturnRecord>
     */
    IPage<ReturnRecord> findReturnRecords(QueryRequest request, ReturnRecordQuery returnRecord);

    /**
     * 查询（所有）
     *
     * @param returnRecord returnRecord
     * @return List<ReturnRecord>
     */
    List<ReturnRecord> findReturnRecords(ReturnRecordQuery returnRecord);

    /**
     * 新增
     *
     * @param returnRecord returnRecord
     */
    void createReturnRecord(ReturnRecord returnRecord);

    /**
     * 修改
     *
     * @param returnRecord returnRecord
     */
    void updateReturnRecord(ReturnRecord returnRecord);

    /**
     * 删除
     *
     * @param returnRecord returnRecord
     */
    void deleteReturnRecord(ReturnRecordQuery returnRecord);

    /**
    * 删除
    *
    * @param returnRecordIds returnRecordIds
    */
    void deleteReturnRecords(String[] returnRecordIds);

    /**
     * 处理上游返回的回执数据
     * @param channelCode 通道标识
     * @param resultData 上游返回数据
     */
	void receiptReturnRecord(String channelCode, String resultData);

	/**
	 * 
	 * @param parseObject
	 */
	void receiptReturnRecord(ReturnRecord parseObject,String msgId);

    Integer countByQueryEntity(ReturnRecordQuery returnRecord);
}
