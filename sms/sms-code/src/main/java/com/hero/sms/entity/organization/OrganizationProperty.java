package com.hero.sms.entity.organization;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户属性 Entity
 *
 * @author Administrator
 * @date 2020-05-01 19:49:03
 */
@Data
@TableName("t_organization_property")
public class OrganizationProperty {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户编号
     */
    @TableField("organization_code")
    private String organizationCode;

    /**
     * 属性类型
     */
    @TableField("property_type")
    private Integer propertyType;

    /**
     * 属性名
     */
    @TableField("name")
    private String name;

    /**
     * 属性值
     */
    @TableField("value")
    private String value;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建日期
     */
    @TableField("create_time")
    private Date createTime;

    public OrganizationProperty(){
        super();
    }
    public OrganizationProperty(String organizationCode, Integer propertyType, String name, String value, String remark) {
        this.organizationCode = organizationCode;
        this.propertyType = propertyType;
        this.name = name;
        this.value = value;
        this.remark = remark;
        this.createTime = new Date();
    }
}
