package com.hero.sms.entity.message;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hero.sms.common.annotation.IsMobile;

import lombok.Data;

/**
 * 手机号码黑名单 Entity
 *
 * @author Administrator
 * @date 2020-03-17 01:17:22
 */
@Data
@TableName("t_mobile_black")
public class MobileBlack {

    /**
     * 号码区域
     */
    @TableId(value = "area")
    private String area;

    /**
     * 手机号码
     */
    @TableId(value = "number")
    @IsMobile(message="请填写正确手机号码")
    private String number;

}
