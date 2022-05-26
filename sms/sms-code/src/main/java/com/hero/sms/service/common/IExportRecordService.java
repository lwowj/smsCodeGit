package com.hero.sms.service.common;

import com.hero.sms.entity.common.ExportRecord;

import com.hero.sms.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.entity.common.ExportRecordQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.exportModel.SendRecordExtExcel;

import java.util.List;

/**
 * 导出文件记录表 Service接口
 *
 * @author Administrator
 * @date 2020-05-26 11:31:36
 */
public interface IExportRecordService extends IService<ExportRecord> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param exportRecord exportRecord
     * @return IPage<ExportRecord>
     */
    IPage<ExportRecord> findExportRecords(QueryRequest request, ExportRecordQuery exportRecord);

    /**
     * 查询（所有）
     *
     * @param exportRecord exportRecord
     * @return List<ExportRecord>
     */
    List<ExportRecord> findExportRecords(ExportRecordQuery exportRecord);

    /**
     * 新增
     *
     * @param exportRecord exportRecord
     */
    void createExportRecord(ExportRecord exportRecord);

    /**
     * 修改
     *
     * @param exportRecord exportRecord
     */
    void updateExportRecord(ExportRecord exportRecord);

    /**
     * 删除
     *
     * @param exportRecord exportRecord
     */
    void deleteExportRecord(ExportRecordQuery exportRecord);

    /**
    * 删除
    *
    * @param exportRecordIds exportRecordIds
    */
    void deleteExportRecords(String[] exportRecordIds);

    boolean exportSendRecord(ExportRecord exportRecord,List sendRecords);
}
