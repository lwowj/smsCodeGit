package com.hero.sms.entity.agent;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 代理系统配置 Entity
 *
 * @author Administrator
 * @date 2020-03-21 01:43:35
 */
@Data
@TableName("t_agent_system_config")
public class AgentSystemConfig {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代理ID
     */
    @TableField("agent_id")
    private Integer agentId;

    /**
     * 平台名称
     */
    @TableField("system_name")
    private String systemName;

    /**
     * 平台LOGO
     */
    @TableField("system_logo_url")
    private String systemLogoUrl;

    /**
     * 域名地址
     */
    @TableField("system_url")
    private String systemUrl;

    /**
     * 认证状态
     */
    @TableField("approve_state_code")
    private String approveStateCode;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 平台LOGO文件（）
     */
    @TableField(exist = false)
    private MultipartFile logoFile;

}
