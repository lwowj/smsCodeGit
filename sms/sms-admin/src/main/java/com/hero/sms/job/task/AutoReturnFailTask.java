package com.hero.sms.job.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.message.ISendRecordService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("autoReturnFailTask")
public class AutoReturnFailTask {

    @Autowired
    private ISendRecordService sendRecordService;

    public  void run(){
        //3天前，状态为提交成功的发送记录

        LocalDateTime start = LocalDateTime.of(LocalDate.now().minusDays(3), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now().minusDays(3),LocalTime.MAX);
        SendRecordQuery query = new SendRecordQuery();
        query.setCreateStartTime(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()));
        query.setCreateEndTime(Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        query.setState(SendRecordStateEnums.ReqSuccess.getCode());

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setPageSize(1000);

        loopQuery(queryRequest,query);

    }

    private void loopQuery(QueryRequest queryRequest, SendRecordQuery query) {
        IPage<SendRecord> sendRecords = sendRecordService.findSendRecords(queryRequest, query);
        if (sendRecords != null && CollectionUtils.isNotEmpty(sendRecords.getRecords())) {
            List<SendRecord> records = sendRecords.getRecords();
            this.sendRecordService.batchUpdateMsgReturnState(records,false);
            long size = sendRecords.getPages();
            if (size > 1){
                loopQuery(queryRequest,query);
            }
        }
    }


}
