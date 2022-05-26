package com.hero.sms.entity.message;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SendBoxQuery extends SendBox {

	/**
     * 是否定时
     */
    private Boolean isTiming;
    
	/**
     * 是否定时（用于查询）
     */
    private Boolean isTimingFlag;
    
	/**
     * 是否已分拣
     */
    private Boolean isSortingFlag;
    
    /**
     * 小于等于定时时间
     */
    private Date leTimingTime;
    
    private Boolean isNeedSmsNumber = false;

    /**
     * 分拣开始时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date sortingStartTime;

    /**
     * 分拣结束时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date sortingEndTime;


    /**
     * 提交开始时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createStartTime;

    /**
     * 提交结束时间 查询条件
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createEndTime;
    /**
     * 号码文件（excel/txt）
     */
    private MultipartFile moblieFile;

    /**
     * 状态除外
     */
    private Integer excludeAuditState;

    private List<Long> ids;
    
    /**
     * 审核状态
     */
    private Integer auditState;
    
    /**
     * token
     */
    private String sessionToken;
    
    /**
     * 是否长短信
     * 1 不是
     * 2 是
     */
    private Integer isLongSms; 
    
}
