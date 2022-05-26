package com.hero.sms.entity.message;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 发件箱 Entity
 *
 * @author Administrator
 * @date 2020-03-07 15:56:53
 */
@Data
@TableName("t_send_box")
public class SendBox extends BaseSend{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8738269252279682816L;

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
     * 发件箱类型
     */
    @TableField("type")
    private Integer type;

    /**
     * 消费金额
     */
    @TableField("consume_amount")
    private Integer consumeAmount;

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
     * 客户端ip
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 定时时间
     */
    @TableField("timing_time")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date timingTime;

    /**
     * 分拣时间
     */
    @TableField("sorting_time")
    private Date sortingTime;

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
    @TableField("create_time")
    private Date createTime;

}
