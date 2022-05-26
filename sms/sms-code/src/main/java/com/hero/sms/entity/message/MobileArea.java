package com.hero.sms.entity.message;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 *  Entity
 *
 * @author Administrator
 * @date 2020-03-24 21:15:09
 */
@Data
@TableName("mobile_area")
public class MobileArea {

    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField("MobileNumber")
    private String mobilenumber;

    /**
     * 
     */
    @TableField("MobileArea")
    private String mobilearea;

    /**
     * 
     */
    @TableField("MobileType")
    private String mobiletype;

    /**
     * 
     */
    @TableField("AreaCode")
    private String areacode;

    /**
     * 
     */
    @TableField("PostCode")
    private String postcode;

    /**
     * 省级代码 0311,010,省会的电话区号
     */
    @TableField("Province_Code")
    private String provinceCode;

    /**
     * 
     */
    @TableField("Create_Date")
    private Date createDate;

}
