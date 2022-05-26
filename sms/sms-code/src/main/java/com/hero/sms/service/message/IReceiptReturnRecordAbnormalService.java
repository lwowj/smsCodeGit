package com.hero.sms.service.message;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormal;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormalQuery;

/**
 * 接收回执信息异常表 Service接口
 *
 * @author Administrator
 * @date 2020-11-21 17:50:39
 */
public interface IReceiptReturnRecordAbnormalService extends IService<ReceiptReturnRecordAbnormal> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param receiptReturnRecordAbnormal receiptReturnRecordAbnormal
     * @return IPage<ReceiptReturnRecordAbnormal>
     */
    IPage<ReceiptReturnRecordAbnormal> findReceiptReturnRecordAbnormals(QueryRequest request, ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal);

    /**
     * 查询（所有）
     *
     * @param receiptReturnRecordAbnormal receiptReturnRecordAbnormal
     * @return List<ReceiptReturnRecordAbnormal>
     */
    List<ReceiptReturnRecordAbnormal> findReceiptReturnRecordAbnormals(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal);

    /**
     * 新增
     *
     * @param receiptReturnRecordAbnormal receiptReturnRecordAbnormal
     */
    void createReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal);

    /**
     * 修改
     *
     * @param receiptReturnRecordAbnormal receiptReturnRecordAbnormal
     */
    void updateReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal);

    /**
     * 删除
     *
     * @param receiptReturnRecordAbnormal receiptReturnRecordAbnormal
     */
    void deleteReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal);

    /**
    * 删除
    *
    * @param receiptReturnRecordAbnormalIds receiptReturnRecordAbnormalIds
    */
    void deleteReceiptReturnRecordAbnormals(String[] receiptReturnRecordAbnormalIds);
}
