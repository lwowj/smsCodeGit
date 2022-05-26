package com.hero.sms.entity.agent;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代理菜单关联表 Entity
 *
 * @author Administrator
 * @date 2020-03-06 10:05:54
 */
@Data
@TableName("t_agent_menu_limit")
public class AgentMenuLimit {

    /**
     * 代理ID
     */
    @TableField("AGENT_ID")
    private Long agentId;

    /**
     * 菜单/按钮ID
     */
    @TableField("MENU_ID")
    private Long menuId;

}
