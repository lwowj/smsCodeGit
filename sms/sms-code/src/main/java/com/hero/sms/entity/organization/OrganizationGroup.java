package com.hero.sms.entity.organization;

import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 商户分组表 Entity
 *
 * @author Administrator
 * @date 2020-06-20 22:38:28
 */
@Data
@TableName("t_organization_group")
public class OrganizationGroup {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户组ID
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 商户code
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 
     */
    @TableField("create_time")
    private Date createTime;

}
