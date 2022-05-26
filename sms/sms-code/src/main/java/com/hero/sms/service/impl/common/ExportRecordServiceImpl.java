package com.hero.sms.service.impl.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.common.ExportRecord;
import com.hero.sms.entity.common.ExportRecordQuery;
import com.hero.sms.entity.message.exportModel.SendRecordExcel;
import com.hero.sms.entity.message.exportModel.SendRecordExtExcel;
import com.hero.sms.enums.common.UserTypeEnums;
import com.hero.sms.mapper.common.ExportRecordMapper;
import com.hero.sms.service.common.IExportRecordService;
import com.wuwenze.poi.ExcelKit;

/**
 * 导出文件记录表 Service实现
 *
 * @author Administrator
 * @date 2020-05-26 11:31:36
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ExportRecordServiceImpl extends ServiceImpl<ExportRecordMapper, ExportRecord> implements IExportRecordService {

    @Autowired
    private ExportRecordMapper exportRecordMapper;

    @Override
    public IPage<ExportRecord> findExportRecords(QueryRequest request, ExportRecordQuery exportRecord) {
        LambdaQueryWrapper<ExportRecord> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(exportRecord.getType())){
            queryWrapper.eq(ExportRecord::getType,exportRecord.getType());
        }
        if (exportRecord.getUserType() != null){
            queryWrapper.eq(ExportRecord::getUserType,exportRecord.getUserType());
        }
        if (exportRecord.getUserId() != null){
            queryWrapper.eq(ExportRecord::getUserId,exportRecord.getUserId());
        }
        if (exportRecord.getCreateStartTime() != null){
            queryWrapper.ge(ExportRecord::getCreateTime,exportRecord.getCreateStartTime());
        }
        if (exportRecord.getCreateEndTime() != null){
            queryWrapper.le(ExportRecord::getCreateTime,exportRecord.getCreateEndTime());
        }

        Page<ExportRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<ExportRecord> findExportRecords(ExportRecordQuery exportRecord) {
	    LambdaQueryWrapper<ExportRecord> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (StringUtils.isNotBlank(exportRecord.getType())){
            queryWrapper.eq(ExportRecord::getType,exportRecord.getType());
        }
        if (exportRecord.getUserType() != null){
            queryWrapper.eq(ExportRecord::getUserType,exportRecord.getUserType());
        }
        if (exportRecord.getUserId() != null){
            queryWrapper.eq(ExportRecord::getUserId,exportRecord.getUserId());
        }
        if (exportRecord.getCreateStartTime() != null){
            queryWrapper.ge(ExportRecord::getCreateTime,exportRecord.getCreateStartTime());
        }
        if (exportRecord.getCreateEndTime() != null){
            queryWrapper.le(ExportRecord::getCreateTime,exportRecord.getCreateEndTime());
        }
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createExportRecord(ExportRecord exportRecord) {
        this.save(exportRecord);
    }

    @Override
    @Transactional
    public void updateExportRecord(ExportRecord exportRecord) {
        this.saveOrUpdate(exportRecord);
    }

    @Override
    @Transactional
    public void deleteExportRecord(ExportRecordQuery exportRecord) {
        LambdaQueryWrapper<ExportRecord> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteExportRecords(String[] exportRecordIds) {
        List<String> list = Arrays.asList(exportRecordIds);
        this.removeByIds(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exportSendRecord(ExportRecord exportRecord, List sendRecords) {

        Integer userType = exportRecord.getUserType();
        if (userType == null){
            log.error("导出任务失败，用户类型不能为空");
            return false;
        }
        Code code = null;
        Class<?> clazz = null;
        if (userType.intValue() == UserTypeEnums.Admin.getCode().intValue()){
            code = DatabaseCache.getCodeBySortCodeAndCode("System","adminExportPath");
            clazz = SendRecordExtExcel.class;
        }else if (userType.intValue() == UserTypeEnums.Agent.getCode().intValue()){
            code = DatabaseCache.getCodeBySortCodeAndCode("System","agentExportPath");
            clazz = SendRecordExcel.class;
        }else if (userType.intValue() == UserTypeEnums.Organization.getCode().intValue()){
            code = DatabaseCache.getCodeBySortCodeAndCode("System","orgExportPath");
            clazz = SendRecordExcel.class;
        }else {
            log.error("导出任务失败，用户类型错误");
            return false;
        }

        String filePath = code.getName() + exportRecord.getFilename();
        File file = null;
        OutputStream outputStream = null;
        try {
            file = new File(filePath);
            outputStream = new FileOutputStream(file);
            ExcelKit.$Builder(clazz, outputStream).writeXlsx(sendRecords, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //保存导出记录
        exportRecord.setCreateTime(new Date());
        this.save(exportRecord);

        return true;
    }
}
