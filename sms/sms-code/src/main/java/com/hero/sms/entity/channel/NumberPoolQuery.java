package com.hero.sms.entity.channel;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NumberPoolQuery extends NumberPool {

    /**
     * 号码区间 头
     */
    private String poolNumberStart;

    /**
     * 号码区间 尾
     */
    private String poolNumberEnd;

    /**
     * 号码文件
     */
    private MultipartFile numberFile;
}

