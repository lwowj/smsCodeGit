package com.hero.sms.commands;

import org.apache.commons.chain.Command;

public abstract class BaseCommand implements Command {

    //字符串
	/**
	 * 错误信息
	 */
    public static final String STR_ERROR_INFO = "STR_ERROR_INFO";
    /**
     * 返回通知的信息
     */
    public static final String STR_NOTIFY_MSG = "STR_NOTIFY_MSG";
    
    /**
     * 通道标识
     */
	public static String STR_CHANNEL_CODE = "STR_CHANNEL_CODE";

	/**
	 * 指定通道id
	 */
	public static String STR_ASSIGN_CHANNEL_ID = "STR_ASSIGN_CHANNEL_ID";

    /**
     * 指定通道id（电信）
     */
    public static final String STR_ASSIGN_CHANNEL_ID_CTCC = "STR_ASSIGN_CHANNEL_ID_CTCC";
    /**
     * 指定通道id（联通）
     */
    public static final String STR_ASSIGN_CHANNEL_ID_CUCC = "STR_ASSIGN_CHANNEL_ID_CUCC";
    /**
     * 指定通道id（移动）
     */
    public static final String STR_ASSIGN_CHANNEL_ID_CMCC = "STR_ASSIGN_CHANNEL_ID_CMCC";
	/**
	 * 上游返回的数据
	 */
	public static String STR_RESULT_DATA = "STR_RESULT_DATA";

    /**
     * 批次号
     */
    public static final String STR_SENDCODE = "STR_SENDCODE";

    /**
     * 通道id
     */
    public static final String INT_CHANNEL_ID = "INT_CHANNEL_ID";
    
    /**
     * 代理资费
     */
    public static final String INT_AGENT_COST = "INT_AGENT_COST";
    
    /**
     * 代理资费（电信）
     */
    public static final String INT_AGENT_COST_CTCC = "INT_AGENT_COST_CTCC";
    /**
     * 代理资费（联通）
     */
    public static final String INT_AGENT_COST_CUCC = "INT_AGENT_COST_CUCC";
    /**
     * 代理资费（移动）
     */
    public static final String INT_AGENT_COST_CMCC = "INT_AGENT_COST_CMCC";
    
    /**
     * 上级代理资费
     */
    public static final String INT_UPAGENT_COST = "INT_UPAGENT_COST";
    /**
     * 上级代理资费（电信）
     */
    public static final String INT_UPAGENT_COST_CTCC = "INT_UPAGENT_COST_CTCC";
    /**
     * 上级代理资费（联通）
     */
    public static final String INT_UPAGENT_COST_CUCC = "INT_UPAGENT_COST_CUCC";
    /**
     * 上级代理资费（移动）
     */
    public static final String INT_UPAGENT_COST_CMCC = "INT_UPAGENT_COST_CMCC";
    /**
     * 商户资费
     */
    public static final String INT_ORG_COST = "INT_ORG_COST";
    /**
     * 通道资费
     */
    public static final String INT_SMS_CHANNEL_COST = "INT_SMS_CHANNEL_COST";
    /**
     * 通道资费ID
     */
    public static final String INT_SMS_CHANNEL_COST_ID = "INT_SMS_CHANNEL_COST_ID";

    //实体
    /**
     * 接收到MQ的消息推送实体
     */
    public static final String OBJ_REQ_SIMPLENOTE = "OBJ_REQ_SIMPLENOTE";
    /**
     * 基础消息实体
     */
    public static final String OBJ_BASESEND_ENTITY = "OBJ_BASESEND_ENTITY";
    /**
     * 发件箱实体
     */
    public static final String OBJ_SENDBOX_ENTITY = "OBJ_SENDBOX_ENTITY";
    public static final String OBJ_SAVE_SENDBOX_ENTITY = "OBJ_SAVE_SENDBOX_ENTITY";
    /**
     * 发送记录实体
     */
    public static final String OBJ_SAVE_SENDRECORD_ENTITY = "OBJ_SAVE_SENDRECORD_ENTITY";
    /**
     * 短信通道实体
     */
    public static final String OBJ_SMS_CHANNEL = "OBJ_SMS_CHANNEL";
    /**
     * 商户扩展实体
     */
    public static final String OBJ_QUERY_ORG = "OBJ_QUERY_ORG";
    /**
     * 代理扩展实体
     */
    public static final String OBJ_QUERY_AGENT = "OBJ_QUERY_AGENT";
    /**
     * 上级代理扩展实体
     */
    public static final String OBJ_QUERY_UPAGENT = "OBJ_QUERY_UPAGENT";
    /**
     * 顶级代理日发送量redis key
     */
    public static final String TOPAGENT_DAY_INC_KEY = "TOPAGENT_DAY_INC_KEY";
    /**
     * 商户资费实体
     */
    public static final String OBJ_ORG_COST = "OBJ_ORG_COST";
    /**
     * 商户资费实体（电信）
     */
    public static final String OBJ_ORG_COST_CTCC = "OBJ_ORG_COST_CTCC";
    /**
     * 商户资费实体（联通）
     */
    public static final String OBJ_ORG_COST_CUCC = "OBJ_ORG_COST_CUCC";
    /**
     * 商户资费实体（移动）
     */
    public static final String OBJ_ORG_COST_CMCC = "OBJ_ORG_COST_CMCC";
    /**
     * 商户可用余额实体
     */
    public static final String OBJ_ORG_AVAILABLE_AMOUNT = "OBJ_ORG_AVAILABLE_AMOUNT";
    /**
     * 通道集合
     */
    public static final String LIST_SMS_CHANNEL = "LIST_SMS_CHANNEL";
    /**
     * 发送记录集合
     */
    public static final String LIST_SEND_RECORD = "LIST_SEND_RECORD";
    /**
     * 回执记录集合
     */
    public static final String LIST_RETURN_RECORD = "LIST_RETURN_RECORD";
    /**
     * EXCEL记录集合
     */
    public static final String LIST_SAVE_MODEL = "LIST_SAVE_MODEL";
    /**
     * api发件箱实体
     */
    public static final String OBJ_API_SENDBOX = "OBJ_API_SENDBOX";
    /**
     * 回执实体
     */
    public static final String OBJ_RETURN_RECORD = "OBJ_RETURN_RECORD";
    //服务
    /**
     * 商户服务接口
     */
    public static final String OBJ_ORG_SERVICE = "OBJ_ORG_SERVICE";
    /**
     * 代理服务接口
     */
    public static final String OBJ_AGENT_SERVICE = "OBJ_AGENT_SERVICE";
    /**
     * 发件箱服务接口
     */
    public static final String OBJ_SENDBOX_SERVICE ="OBJ_SENDBOX_SERVICE" ;
    /**
     * 发送记录服务接口
     */
    public static final String OBJ_SENDRECORD_SERVICE ="OBJ_SENDRECORD_SERVICE" ;
    
    /**
     * 接收回执异常信息处理接口
     */
    public static final String OBJ_RECEIPT_RETURN_RECORD_SERVICE ="OBJ_RECEIPT_RETURN_RECORD_SERVICE" ;
    /**
     * 通道服务接口
     */
    public static final String OBJ_CHANNEL_SERVICE = "OBJ_CHANNEL_SERVICE";
    /**
     * 通道资费服务接口
     */
    public static final String OBJ_CHANNEL_COST_SERVICE = "OBJ_CHANNEL_COST_SERVICE";
    /**
     * 消息推送服务接口
     */
    public static final String OBJ_PUSH_SERVICE = "OBJ_PUSH_SERVICE";

    /**
     * 回执记录服务接口
     */
	public static final String OBJ_RETURN_RECORD_SERVICE = "OBJ_RETURN_RECORD_SERVICE";

	/**
	 * 消息模板服务接口
	 */
	public static final String OBJ_TEMPLATE_SERVICE = "OBJ_TEMPLATE_SERVICE";
	
	/**
	 * 手机黑名单服务接口
	 */
	public static Object OBJ_MOBILEBLACK_SERVICE = "OBJ_MOBILEBLACK_SERVICE";
	
	/**
	 * 手机地区服务接口
	 */
	public static Object OBJ_MOBILEAREA_SERVICE = "OBJ_MOBILEAREA_SERVICE";
	
	/**
	 * 发件箱分拣校验服务接口
	 */
	public static Object OBJ_SENDBOXRECORDCHECKINFO_SERVICE = "OBJ_SENDBOXRECORDCHECKINFO_SERVICE";
}
