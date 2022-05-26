package com.hero.sms.entity.message.history;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 历史发件箱 Entity
 *
 * @author Administrator
 * @date 2020-03-15 23:31:27
 */
@Data
@TableName("t_send_box_history")
public class SendBoxHistory {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 批次号
     */
    @TableField("send_code")
    private String sendCode;

    /**
     * 上级代理商id
     */
    @TableField("up_agent_id")
    private Integer upAgentId;
    
    /**
     * 代理商id
     */
    @TableField("agent_id")
    private Integer agentId;

    /**
     * 商户编号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 消息类型
     */
    @TableField("sms_type")
    private Integer smsType;

    /**
     * 发件箱类型
     */
    @TableField("type")
    private Integer type;

    /**
     * 消息内容
     */
    @TableField("sms_content")
    private String smsContent;

    /**
     * 手机号码归属国家编码
     */
    @TableField("sms_number_area")
    private String smsNumberArea;

    /**
     * 接收的手机号码集合(逗号隔开)
     */
    @TableField("sms_numbers")
    private String smsNumbers;

    /**
     * 号码数
     */
    @TableField("number_count")
    private Integer numberCount;

    /**
     * 有效的短信数
     */
    @TableField("sms_count")
    private Integer smsCount;

    /**
     * 短信字数
     */
    @TableField("sms_words")
    private Integer smsWords;

    /**
     * 审核状态
     */
    @TableField("audit_state")
    private Integer auditState;

    /**
     * 拒绝原因
     */
    @TableField("refuse_cause")
    private String refuseCause;

    /**
     * 提交方式
     */
    @TableField("sub_type")
    private Integer subType;

    /**
     * 客户端ip
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 定时时间
     */
    @TableField("timing_time")
    private Date timingTime;

    /**
     * 分拣时间
     */
    @TableField("sorting_time")
    private Date sortingTime;

    /**
     * 消费金额
     */
    @TableField("consume_amount")
    private Integer consumeAmount;

    /**
     * 通道成本
     */
    @TableField("channel_cost_amount")
    private Integer channelCostAmount;
    
    /**
     * 上级代理商收益
     */
    @TableField("up_agent_income_amount")
    private Integer upAgentIncomeAmount;
    
    /**
     * 代理商收益
     */
    @TableField("agent_income_amount")
    private Integer agentIncomeAmount;

    /**
     * 平台收益
     */
    @TableField("income_amount")
    private Integer incomeAmount;

    /**
     * 提交人
     */
    @TableField("create_username")
    private String createUsername;

    /**
     * 定时时间(固定值，用于区分定时与及时短信：原定时字段发送后会清空，无法判断)
     * 2021-01-28 新增定时固定显示字段
     */
    @TableField("is_timing_time")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date isTimingTime;
    
    /**
     * 创建时间
     */
    @TableId(value = "create_time", type = IdType.AUTO)
    private Date createTime;

}
