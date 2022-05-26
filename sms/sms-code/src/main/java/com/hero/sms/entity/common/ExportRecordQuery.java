package com.hero.sms.entity.common;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ExportRecordQuery extends ExportRecord {

    /**
     * 创建起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createStartTime;

    /**
     * 创建结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createEndTime;

}
