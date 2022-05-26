package com.hero.sms.entity.organization;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 商户用户菜单关联表 Entity
 *
 * @author Administrator
 * @date 2020-03-08 00:13:33
 */
@Data
@TableName("t_organization_user_limit")
public class OrganizationUserLimit {

    /**
     * 商户用户ID
     */
    @TableField("USER_ID")
    private Long userId;

    /**
     * 菜单/按钮ID
     */
    @TableField("MENU_ID")
    private Long menuId;

}
